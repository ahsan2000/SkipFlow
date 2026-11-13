package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean

/** Owns every native overlay View and callback on one dedicated UI Looper.
 * Accessibility tree Binder calls on the service thread cannot stall input here.
 */
class OverlayHost(private val service: AccessibilityService,private val prefs: SharedPreferences) {
    private val thread=HandlerThread("SkipFlowOverlay",Process.THREAD_PRIORITY_DISPLAY).apply {start()}
    private val handler=Handler(thread.looper)
    private val closed=AtomicBoolean(false)
    @Volatile private var controls: FloatingControls?=null
    private var celebration: SkipCelebration?=null
    val isInteracting: Boolean get()=controls?.isInteracting==true
    private fun unlocked()=(service.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive &&
        !(service.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
    private val guard=object:Runnable {
        override fun run() {
            if(closed.get()) return
            val available=unlocked()
            controls?.setVisible(available && prefs.getBoolean("floating_controls",true))
            if(!available) celebration?.clear()
            handler.postDelayed(this,500)
        }
    }
    init {
        handler.post {
            controls=FloatingControls(service,prefs)
            celebration=SkipCelebration(service)
            guard.run()
        }
    }
    private fun post(action:()->Unit) {if(!closed.get()) handler.post {if(!closed.get()) action()}}
    fun setVisible(show: Boolean)=post {controls?.setVisible(show && unlocked() && prefs.getBoolean("floating_controls",true))}
    fun refresh()=post {controls?.refresh()}
    fun configurationChanged()=post {controls?.configurationChanged();celebration?.clear()}
    fun showCelebration(previous: Int,preview: Boolean=false)=post {
        if(unlocked() && (preview || prefs.getBoolean("skip_animation",true))) celebration?.show(previous,preview)
    }
    fun clearCelebration()=post {celebration?.clear()}
    fun close() {
        if(!closed.compareAndSet(false,true)) return
        handler.removeCallbacksAndMessages(null)
        handler.post {
            controls?.close();controls=null
            celebration?.clear();celebration=null
            thread.quitSafely()
        }
    }
}
