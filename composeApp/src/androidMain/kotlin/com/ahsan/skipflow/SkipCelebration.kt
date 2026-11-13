package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** A small, touch-through receipt; preview never increments real statistics. */
class SkipCelebration(private val service: AccessibilityService) {
    private val wm=service.getSystemService(WindowManager::class.java)
    private val handler=Handler(requireNotNull(Looper.myLooper()))
    private fun dp(value: Int)=(value*service.resources.displayMetrics.density).toInt()
    private var current: View?=null
    private val pending=java.util.ArrayDeque<Pair<Int,Boolean>>()
    private val mint=Color.rgb(120,228,196)
    fun show(previous: Int,preview: Boolean=false) {
        if(current!=null) {pending.add(previous to preview);return}
        display(previous,preview)
    }
    private fun display(previous: Int,preview: Boolean) {
        val box=LinearLayout(service).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(dp(22),dp(12),dp(22),dp(14))
            background=GradientDrawable().apply {setColor(Color.rgb(20,30,43));cornerRadius=dp(22).toFloat();setStroke(dp(1),Color.rgb(60,99,92))}
            elevation=dp(8).toFloat()
            importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        val heading=TextView(service).apply {
            text=if(preview) "ANIMATION PREVIEW" else "AD SKIPPED"
            textSize=10f;letterSpacing=.14f;setTextColor(mint)
            typeface=service.resources.getFont(R.font.lato_bold)
            gravity=Gravity.CENTER
        }
        val equation=LinearLayout(service).apply {orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(0,dp(7),0,0)}
        fun number(text: String,color: Int,bold: Boolean=true)=TextView(service).apply {
            this.text=text;textSize=24f;setTextColor(color)
            typeface=service.resources.getFont(if(bold) R.font.lato_bold else R.font.lato_regular)
            setPadding(dp(3),0,dp(3),0)
        }
        val one=number("1",mint)
        val prior=number(" + $previous = ",Color.rgb(154,171,191),false)
        val total=number((previous+1).toString(),Color.rgb(242,246,252))
        equation.addView(one);equation.addView(prior);equation.addView(total)
        box.addView(heading);box.addView(equation)
        val params=WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL;y=dp(70);setTitle("SkipFlow skip celebration")}
        wm.addView(box,params);current=box
        val animated=ValueAnimator.areAnimatorsEnabled()
        if(animated) {
            box.alpha=0f;box.translationY=-dp(12).toFloat();box.scaleX=.9f;box.scaleY=.9f
            prior.alpha=0f;total.alpha=0f
            box.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f).setDuration(200).start()
            handler.postDelayed({prior.animate().alpha(1f).setDuration(180).start()},250)
            handler.postDelayed({
                total.alpha=1f;total.scaleX=.8f;total.scaleY=.8f;total.setTextColor(mint)
                total.animate().scaleX(1f).scaleY(1f).setDuration(240).start()
            },500)
        }
        handler.postDelayed({
            if(animated) box.animate().alpha(0f).translationY(-dp(8).toFloat()).setDuration(180).withEndAction {remove(box)}.start()
            else remove(box)
        },2500)
    }
    private fun remove(view: View) {
        if(current!==view) return
        view.animate().cancel()
        wm.removeView(view);current=null
        if(pending.isNotEmpty()) {val(next,preview)=pending.removeFirst();display(next,preview)}
    }
    fun clear() {
        handler.removeCallbacksAndMessages(null);pending.clear()
        current?.let {it.animate().cancel();wm.removeView(it)}
        current=null
    }
}
