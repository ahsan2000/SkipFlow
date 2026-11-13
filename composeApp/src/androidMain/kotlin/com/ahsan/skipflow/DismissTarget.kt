package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityService
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** Touch-through drop target: dropping hides only floating controls, not automation. */
class DismissTarget(private val service: AccessibilityService) {
    private val wm=service.getSystemService(WindowManager::class.java)
    private fun dp(value: Int)=(value*service.resources.displayMetrics.density).toInt()
    private var shown=false
    private var hovered=false
    private val location=IntArray(2)
    private val ring=object:View(service) {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            val x=width/2f;val y=height/2f;val r=width*.43f
            paint.style=Paint.Style.FILL
            paint.color=if(hovered) Color.rgb(139,42,57) else Color.rgb(42,27,36)
            canvas.drawCircle(x,y,r,paint)
            paint.style=Paint.Style.STROKE;paint.strokeWidth=dp(2).toFloat()
            paint.color=if(hovered) Color.rgb(255,178,184) else Color.rgb(170,87,102)
            canvas.drawCircle(x,y,r,paint)
            paint.strokeWidth=dp(3).toFloat();paint.strokeCap=Paint.Cap.ROUND
            paint.color=Color.rgb(255,222,225)
            val d=dp(9).toFloat()
            canvas.drawLine(x-d,y-d,x+d,y+d,paint)
            canvas.drawLine(x-d,y+d,x+d,y-d,paint)
        }
    }
    private val label=TextView(service).apply {
        text="Drop to hide";textSize=12f;gravity=Gravity.CENTER
        setTextColor(Color.rgb(255,222,225))
        typeface=service.resources.getFont(R.font.lato_bold)
        setPadding(0,dp(6),0,dp(6))
    }
    private val root=LinearLayout(service).apply {
        orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL
        setPadding(0,dp(4),0,dp(4))
        addView(ring,LinearLayout.LayoutParams(dp(72),dp(72)))
        addView(label,LinearLayout.LayoutParams(dp(160),WindowManager.LayoutParams.WRAP_CONTENT))
        importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }
    private val params=WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        PixelFormat.TRANSLUCENT
    ).apply {gravity=Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL;y=dp(28);setTitle("SkipFlow close target")}
    fun show() {
        if(shown) return
        hovered=false;label.text="Drop to hide";ring.invalidate()
        wm.addView(root,params);shown=true
    }
    fun update(iconX: Float,iconY: Float): Boolean {
        if(!shown || !ring.isLaidOut) return false
        ring.getLocationOnScreen(location)
        val dx=iconX-(location[0]+ring.width/2f)
        val dy=iconY-(location[1]+ring.height/2f)
        val hit=dx*dx+dy*dy<=dp(52).toFloat()*dp(52)
        if(hit!=hovered) {
            hovered=hit;ring.invalidate()
            label.text=if(hit) "Release to hide" else "Drop to hide"
            if(hit) ring.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
        }
        return hit
    }
    fun hide() {
        if(!shown) return
        wm.removeView(root);shown=false;hovered=false
    }
}
