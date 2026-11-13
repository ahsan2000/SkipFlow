package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.roundToInt

/** Small, draggable Accessibility overlay; no SYSTEM_ALERT_WINDOW permission required. */
class FloatingControls(private val service: AccessibilityService, private val prefs: SharedPreferences) {
    private val wm = service.getSystemService(WindowManager::class.java)
    private val handler = Handler(requireNotNull(Looper.myLooper()))
    private val density = service.resources.displayMetrics.density
    private fun dp(value: Int) = (value*density).roundToInt()
    private val mint = Color.rgb(120,228,196)
    private val ink = Color.rgb(20,30,43)
    private var visible = false
    private var expanded = false
    @Volatile var isInteracting = false
        private set
    private var anchorX = 0
    private var anchorY = 0
    private var initializedPosition = false
    private var snap: ValueAnimator? = null
    private var docking: ValueAnimator? = null
    private var dockProgress = 0f
    private var cachedBounds: android.graphics.Rect? = null
    private val dragSlop = minOf(dp(2),ViewConfiguration.get(service).scaledTouchSlop).coerceAtLeast(1)
    private var moveScheduled = false
    private val moveFrame = Runnable {
        moveScheduled=false
        if(visible) position()
    }
    private val root = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }
    private val dismissTarget = DismissTarget(service)
    private val bubble = Bubble()
    // Clipping inside a half-width window also shrinks the touch footprint.
    private val bubbleHost = FrameLayout(service).apply {
        clipChildren=true; clipToPadding=true
        addView(bubble,FrameLayout.LayoutParams(dp(56),dp(56)))
    }
    private val menu = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(6),dp(4),dp(6),dp(4))
        background = GradientDrawable().apply { setColor(ink); cornerRadius=dp(18).toFloat(); setStroke(dp(1),Color.rgb(50,71,85)) }
        elevation = dp(6).toFloat()
    }
    private val pause = action("Pause",false) {
        prefs.edit().putBoolean("enabled",!prefs.getBoolean("enabled",false)).apply()
        refresh()
    }
    private val home = action("Home",true) {
        expanded=false
        render()
        service.startActivity(Intent(service,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity=Gravity.TOP or Gravity.LEFT; setTitle("SkipFlow floating controls") }
    private val collapse = Runnable { expanded=false; render(); scheduleIdle() }
    private val idle = Runnable {
        if(visible && !expanded && !bubble.isAccessibilityFocused) animateDock(1f)
    }

    init {
        // The docked handle lies in Android's Back-swipe zone. Reserve only its
        // visible bounds so one uninterrupted press/drag reaches our listener.
        if (Build.VERSION.SDK_INT >= 29) {
            bubbleHost.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
                view.systemGestureExclusionRects = listOf(android.graphics.Rect(0,0,view.width,view.height))
            }
        }
        menu.addView(pause,LinearLayout.LayoutParams(dp(62),dp(54)))
        menu.addView(home,LinearLayout.LayoutParams(dp(62),dp(54)))
        bubble.setOnClickListener {
            snap?.cancel()
            handler.removeCallbacks(idle)
            handler.removeCallbacks(collapse)
            if(dockProgress>0f) {
                animateDock(0f) {
                    expanded=true; render(); handler.postDelayed(collapse,6000)
                }
            } else {
                expanded=!expanded
                render()
                if (expanded) handler.postDelayed(collapse,6000) else scheduleIdle()
            }
        }
        var downX=0f; var downY=0f; var startX=0; var startY=0; var dragged=false
        var worstInputLag=0L
        bubble.setOnTouchListener { _,event ->
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isInteracting=true
                    worstInputLag=(android.os.SystemClock.uptimeMillis()-event.eventTime).coerceAtLeast(0)
                    snap?.cancel(); docking?.cancel()
                    handler.removeCallbacks(collapse); handler.removeCallbacks(idle)
                    // Prepare the full-size drag surface before movement, not on its first frame.
                    // Keep this same view attached so the held pointer stays captured.
                    if(dockProgress>0f) {dockProgress=0f;position()}
                    downX=event.rawX; downY=event.rawY; startX=anchorX; startY=anchorY; dragged=false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx=event.rawX-downX; val dy=event.rawY-downY
                    if (dx*dx+dy*dy>dragSlop*dragSlop) dragged=true
                    if (dragged) {
                        dismissTarget.show()
                        worstInputLag=maxOf(worstInputLag,android.os.SystemClock.uptimeMillis()-event.eventTime)
                        if (expanded) { expanded=false; render() }
                        anchorX=startX+dx.roundToInt(); anchorY=startY+dy.roundToInt()
                        clamp(); requestMove()
                        dismissTarget.update(anchorX+dp(28).toFloat(),anchorY+dp(28).toFloat())
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isInteracting=false
                    flushMove()
                    val hide=dragged && dismissTarget.update(anchorX+dp(28).toFloat(),anchorY+dp(28).toFloat())
                    dismissTarget.hide()
                    if(hide) {
                        // Remove immediately on this UI thread; Accessibility may be busy.
                        prefs.edit().putBoolean("floating_controls",false).apply()
                        initializedPosition=false
                        setVisible(false)
                    } else if (dragged) snapToEdge() else bubble.performClick()
                    if(service.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                        android.util.Log.i("SkipFlowDrag","thread=${Thread.currentThread().name} maxInputLagMs=$worstInputLag dragged=$dragged dismissed=$hide")
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isInteracting=false;flushMove();dismissTarget.hide()
                    if (dragged) snapToEdge() else scheduleIdle();true
                }
                else -> false
            }
        }
    }
    private fun action(label: String, house: Boolean, callback: ()->Unit): TextView = TextView(service).apply {
        text=label
        gravity=Gravity.CENTER
        textSize=11f
        setTextColor(Color.rgb(242,246,252))
        typeface=service.resources.getFont(R.font.lato_bold)
        contentDescription=if(house) "Open SkipFlow app" else "Pause SkipFlow automation"
        setPadding(dp(2),dp(3),dp(2),dp(2))
        setOnClickListener { callback(); this@FloatingControls.handler.removeCallbacks(collapse); if(expanded) this@FloatingControls.handler.postDelayed(collapse,6000) else scheduleIdle() }
        isClickable=true; isFocusable=true
        background=service.getDrawable(android.R.drawable.list_selector_background)
        setCompoundDrawablesWithIntrinsicBounds(null,ControlGlyph(house,false),null,null)
        compoundDrawablePadding=dp(3)
    }
    @Suppress("DEPRECATION")
    private fun readBounds(): android.graphics.Rect {
        if(Build.VERSION.SDK_INT>=30) {
            val metrics=wm.currentWindowMetrics
            val inset=metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            return android.graphics.Rect(inset.left+dp(8),inset.top+dp(8),metrics.bounds.width()-inset.right-dp(8),metrics.bounds.height()-inset.bottom-dp(8))
        }
        val metrics=android.util.DisplayMetrics(); wm.defaultDisplay.getRealMetrics(metrics)
        return android.graphics.Rect(dp(8),dp(32),metrics.widthPixels-dp(8),metrics.heightPixels-dp(40))
    }
    private fun bounds(): android.graphics.Rect = cachedBounds ?: readBounds().also {cachedBounds=it}
    private fun requestMove() {
        if(!moveScheduled) {moveScheduled=true;root.postOnAnimation(moveFrame)}
    }
    private fun flushMove() {
        if(moveScheduled) {root.removeCallbacks(moveFrame);moveScheduled=false;if(visible) position()}
    }
    private fun clamp() {
        val area=bounds()
        anchorX=anchorX.coerceIn(area.left,maxOf(area.left,area.right-dp(56)))
        anchorY=anchorY.coerceIn(area.top,maxOf(area.top,area.bottom-dp(56)))
    }
    private fun loadPosition() {
        val area=bounds()
        anchorX=area.left+((area.width()-dp(56)).coerceAtLeast(0)*prefs.getFloat("floating_x",1f)).roundToInt()
        anchorY=area.top+((area.height()-dp(56)).coerceAtLeast(0)*prefs.getFloat("floating_y",.4f)).roundToInt()
        initializedPosition=true; clamp()
    }
    private fun savePosition() {
        val area=bounds()
        prefs.edit().putFloat("floating_x",(anchorX-area.left).toFloat()/(area.width()-dp(56)).coerceAtLeast(1))
            .putFloat("floating_y",(anchorY-area.top).toFloat()/(area.height()-dp(56)).coerceAtLeast(1)).apply()
    }
    private fun onRight() = anchorX+dp(28)>bounds().centerX()
    private fun position() {
        val progress=if(expanded) 0f else dockProgress
        val right=onRight()
        val area=bounds()
        val shownWidth=(dp(56)-dp(28)*progress).roundToInt()
        bubbleHost.layoutParams?.let {
            if(it.width!=shownWidth) { it.width=shownWidth; bubbleHost.layoutParams=it }
        }
        bubble.translationX=if(right) 0f else -dp(28)*progress
        val tuckedX=if(right) area.right+dp(8)-dp(28) else area.left-dp(8)
        params.width=if(expanded) WindowManager.LayoutParams.WRAP_CONTENT else shownWidth
        params.x=if(expanded) anchorX-if(right) dp(148) else 0
            else (anchorX+(tuckedX-anchorX)*progress).roundToInt()
        params.y=anchorY
        root.alpha=1f-.45f*progress
        if(visible) wm.updateViewLayout(root,params)
    }
    private fun scheduleIdle() {
        handler.removeCallbacks(idle)
        if(visible && !expanded) handler.postDelayed(idle,3000)
    }
    private fun animateDock(target: Float, finished: (() -> Unit)?=null) {
        docking?.cancel()
        if(!ValueAnimator.areAnimatorsEnabled()) {
            dockProgress=target;position();finished?.invoke();return
        }
        docking=ValueAnimator.ofFloat(dockProgress,target).apply {
            duration=220
            addUpdateListener {dockProgress=it.animatedValue as Float;position()}
            addListener(object:android.animation.AnimatorListenerAdapter() {
                private var cancelled=false
                override fun onAnimationCancel(animation:android.animation.Animator) {cancelled=true}
                override fun onAnimationEnd(animation:android.animation.Animator) {
                    if(!cancelled && visible) finished?.invoke()
                }
            })
            start()
        }
    }
    private fun render() {
        if (!visible) return
        bubble.contentDescription=if(prefs.getBoolean("enabled",false)) "SkipFlow running. Tap for pause and Home. Drag to move." else "SkipFlow paused. Tap for resume and Home. Drag to move."
        bubble.invalidate()
        val enabled=prefs.getBoolean("enabled",false)
        pause.text=if(enabled) "Pause" else "Resume"
        pause.contentDescription=if(enabled) "Pause SkipFlow automation" else "Resume SkipFlow automation"
        pause.setCompoundDrawablesWithIntrinsicBounds(null,ControlGlyph(false,!enabled),null,null)
        root.removeAllViews()
        val spacer=View(service)
        fun addBubble() {root.addView(bubbleHost,LinearLayout.LayoutParams(dp(56),dp(56)))}
        fun addMenu() {root.addView(menu,LinearLayout.LayoutParams(dp(136),dp(62)))}
        if(expanded && onRight()) { addMenu(); root.addView(spacer,LinearLayout.LayoutParams(dp(12),1)); addBubble() }
        else { addBubble(); if(expanded) { root.addView(spacer,LinearLayout.LayoutParams(dp(12),1)); addMenu() } }
        position()
    }
    private fun snapToEdge() {
        val area=bounds(); val target=if(onRight()) area.right-dp(56) else area.left
        if(!ValueAnimator.areAnimatorsEnabled()) {anchorX=target; position(); savePosition(); scheduleIdle(); return}
        snap=ValueAnimator.ofInt(anchorX,target).apply {
            duration=180
            addUpdateListener { anchorX=it.animatedValue as Int; position() }
            addListener(object:android.animation.AnimatorListenerAdapter() {
                private var cancelled=false
                override fun onAnimationCancel(animation:android.animation.Animator) {cancelled=true}
                override fun onAnimationEnd(animation:android.animation.Animator) {if(!cancelled) {savePosition();scheduleIdle()}}
            })
            start()
        }
    }
    fun refresh() { if(visible) {if(isInteracting) bubble.invalidate() else render()} }
    fun setVisible(show: Boolean) {
        if(show==visible) return
        if(show) {
            cachedBounds=null
            if(!initializedPosition) loadPosition()
            clamp(); expanded=false; dockProgress=0f; root.alpha=1f
            params.x=anchorX;params.y=anchorY;params.width=WindowManager.LayoutParams.WRAP_CONTENT
            root.removeAllViews(); root.addView(bubbleHost,LinearLayout.LayoutParams(dp(56),dp(56)))
            wm.addView(root,params);visible=true;render();scheduleIdle()
        } else {
            handler.removeCallbacks(collapse);handler.removeCallbacks(idle)
            root.removeCallbacks(moveFrame);moveScheduled=false;isInteracting=false
            dismissTarget.hide()
            snap?.cancel();docking?.cancel();expanded=false
            wm.removeView(root);visible=false
        }
    }
    fun configurationChanged() {
        handler.removeCallbacks(idle);docking?.cancel();snap?.cancel()
        root.removeCallbacks(moveFrame);moveScheduled=false;cachedBounds=null;isInteracting=false
        dismissTarget.hide()
        expanded=false;dockProgress=0f;loadPosition();render();scheduleIdle()
    }
    fun close() { setVisible(false);handler.removeCallbacksAndMessages(null) }

    private inner class Bubble : View(service) {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        private val logo=service.getDrawable(R.drawable.ic_skipflow_foreground)!!
        override fun performClick(): Boolean = super.performClick()
        override fun onDraw(canvas: Canvas) {
            val radius=width/2f-dp(2)
            paint.style=Paint.Style.FILL;paint.color=ink
            canvas.drawCircle(width/2f,height/2f,radius,paint)
            paint.style=Paint.Style.STROKE;paint.strokeWidth=dp(1).toFloat();paint.color=Color.rgb(68,104,102)
            canvas.drawCircle(width/2f,height/2f,radius,paint)
            logo.setBounds(0,0,width,height);logo.draw(canvas)
            paint.style=Paint.Style.FILL;paint.color=ink
            canvas.drawCircle(width*.8f,height*.8f,dp(7).toFloat(),paint)
            paint.color=if(prefs.getBoolean("enabled",false)) mint else Color.rgb(242,203,139)
            canvas.drawCircle(width*.8f,height*.8f,dp(4).toFloat(),paint)
        }
    }
    private inner class ControlGlyph(private val house: Boolean,private val play: Boolean):android.graphics.drawable.Drawable() {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=mint;strokeWidth=dp(2).toFloat();strokeCap=Paint.Cap.ROUND}
        override fun getIntrinsicWidth()=dp(21)
        override fun getIntrinsicHeight()=dp(21)
        override fun draw(canvas: Canvas) {
            canvas.save();canvas.translate(bounds.left.toFloat(),bounds.top.toFloat());canvas.scale(bounds.width()/24f,bounds.height()/24f)
            paint.style=Paint.Style.STROKE
            if(house) {
                val p=android.graphics.Path().apply {moveTo(3f,11f);lineTo(12f,3f);lineTo(21f,11f);moveTo(6f,10f);lineTo(6f,21f);lineTo(10f,21f);lineTo(10f,15f);lineTo(14f,15f);lineTo(14f,21f);lineTo(18f,21f);lineTo(18f,10f)}
                canvas.drawPath(p,paint)
            } else if(play) {
                paint.style=Paint.Style.FILL
                canvas.drawPath(android.graphics.Path().apply {moveTo(7f,4f);lineTo(20f,12f);lineTo(7f,20f);close()},paint)
            } else {canvas.drawLine(8f,5f,8f,19f,paint);canvas.drawLine(16f,5f,16f,19f,paint)}
            canvas.restore()
        }
        override fun setAlpha(alpha: Int) {paint.alpha=alpha}
        override fun setColorFilter(filter:android.graphics.ColorFilter?) {paint.colorFilter=filter}
        @Deprecated("Deprecated in Java") override fun getOpacity()=PixelFormat.TRANSLUCENT
    }
}
