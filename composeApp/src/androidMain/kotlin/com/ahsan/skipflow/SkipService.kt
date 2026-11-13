package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SkipService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastClick = -2000L
    private var lastScan = 0L
    private var lastReadable = 0L
    private lateinit var volume: AdVolumeController
    private lateinit var floating: OverlayHost
    private lateinit var prefs: SharedPreferences
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key in setOf("enabled","lower_ad_volume","floating_controls","skip_animation",SkipPolicy.YOUTUBE,SkipPolicy.MUSIC)) handler.post {
            if (::floating.isInitialized) floating.refresh()
            if (key == "skip_animation" && !prefs.getBoolean("skip_animation",true)) floating.clearCelebration()
            inspect()
        }
        if (key == "animation_preview") handler.post {
            if (::floating.isInitialized && screenAvailable()) floating.showCelebration(prefs.getInt("count",0),preview=true)
        }
    }
    private val scan = object : Runnable {
        override fun run() { inspect(); handler.postDelayed(this,650) }
    }
    override fun onServiceConnected() {
        prefs = getSharedPreferences("skipflow",Context.MODE_PRIVATE)
        if (::floating.isInitialized) floating.close()
        floating = OverlayHost(this,prefs)
        volume = AdVolumeController(this,prefs)
        volume.recover()
        prefs.unregisterOnSharedPreferenceChangeListener(settingsListener)
        prefs.registerOnSharedPreferenceChangeListener(settingsListener)
        handler.removeCallbacks(scan)
        handler.post(scan)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (::volume.isInitialized && SystemClock.elapsedRealtime()-lastScan > 200) inspect()
    }
    private fun unknownWindow() {
        // Short gaps can occur during ad transitions or Android's volume overlay.
        if (lastScan-lastReadable > 3000) volume.stop()
    }
    private fun screenAvailable() = (getSystemService(POWER_SERVICE) as PowerManager).isInteractive &&
        !(getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::floating.isInitialized) floating.configurationChanged()
        if (::floating.isInitialized) floating.clearCelebration()
    }
    private fun inspect() {
        if (!::volume.isInitialized) return
        lastScan = SystemClock.elapsedRealtime()
        val screenOn=screenAvailable()
        floating.setVisible(screenOn && prefs.getBoolean("floating_controls",true))
        if (!screenOn) floating.clearCelebration()
        if (!prefs.getBoolean("enabled",false) || !screenOn) { volume.stop(); return }
        // Keep blocking Accessibility tree queries off the gesture's UI frames.
        // The normal timer resumes scanning as soon as the handle is released.
        if (floating.isInteracting) return
        val root = rootInActiveWindow ?: run { unknownWindow(); return }
        try {
            val pkg = root.packageName?.toString() ?: run { unknownWindow(); return }
            if (!SkipPolicy.supports(pkg)) {
                if (pkg == "com.android.systemui" || pkg == "android") unknownWindow() else volume.stop()
                return
            }
            if (!prefs.getBoolean(pkg,true)) { volume.stop(); return }
            val lowerVolume = prefs.getBoolean("lower_ad_volume",true)
            if (!lowerVolume) volume.stop()
            val queue = java.util.ArrayDeque<Pair<AccessibilityNodeInfo, Boolean>>()
            queue.add(AccessibilityNodeInfo.obtain(root) to false)
            var visited = 0
            var ad = false
            var badge = false
            var skip: AccessibilityNodeInfo? = null
            try {
                while (queue.isNotEmpty() && visited++ < 1200) {
                    val (node, parentIsPlayer) = queue.removeFirst()
                    val insidePlayer = parentIsPlayer || AdSignals.isPlayer(pkg,node.viewIdResourceName)
                    try {
                        if (node.packageName?.toString() == pkg && node.isVisibleToUser) {
                            val id = node.viewIdResourceName
                            val text = node.text?.toString()
                            val description = node.contentDescription?.toString()
                            ad = ad || AdSignals.isStrongAd(pkg,id,text,description)
                            badge = badge || (insidePlayer && AdSignals.isPlayerBadge(pkg,id,text,description))
                            if (skip == null && node.isEnabled && SkipPolicy.matches(pkg,id,text,description)) skip = AccessibilityNodeInfo.obtain(node)
                        }
                        for (i in 0 until node.childCount) node.getChild(i)?.let { queue.add(it to insidePlayer) }
                    } finally { node.recycle() }
                }
                // The independent overlay can pause while a remote tree query is waiting.
                // Revalidate current settings before applying its result.
                if (!prefs.getBoolean("enabled",false) || !prefs.getBoolean(pkg,true) || !screenAvailable()) {
                    volume.stop();return
                }
                if (ad || badge || queue.isEmpty()) {
                    lastReadable = lastScan
                    if (prefs.getBoolean("lower_ad_volume",true)) volume.observe(ad || badge,lastScan) else volume.stop()
                } else unknownWindow()
                // Volume is lowered before clicking; disappearance of ad controls restores it later.
                if (skip != null && lastScan-lastClick >= 1400 && click(skip!!,pkg)) {
                    lastClick = SystemClock.elapsedRealtime()
                    val previous=prefs.getInt("count",0)
                    if (previous < Int.MAX_VALUE) {
                        prefs.edit().putInt("count",previous+1).apply()
                        if (prefs.getBoolean("skip_animation",true)) floating.showCelebration(previous)
                    }
                }
            } finally {
                skip?.recycle()
                while (queue.isNotEmpty()) queue.removeFirst().first.recycle()
            }
        } finally { root.recycle() }
    }
    private fun click(node: AccessibilityNodeInfo,pkg: String): Boolean {
        var current: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(node)
        try {
            repeat(3) {
                val candidate = current ?: return false
                if (candidate.packageName?.toString()!=pkg || !candidate.isEnabled || !candidate.isVisibleToUser) return false
                if (candidate.isClickable) return candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                val parent = candidate.parent
                candidate.recycle()
                current = parent
            }
            return false
        } finally { current?.recycle() }
    }
    private fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        if (::prefs.isInitialized) prefs.unregisterOnSharedPreferenceChangeListener(settingsListener)
        if (::volume.isInitialized) volume.stop()
        if (::floating.isInitialized) floating.close()
    }
    override fun onInterrupt() { if (::volume.isInitialized) volume.stop() }
    override fun onUnbind(intent: android.content.Intent?): Boolean { cleanup(); return super.onUnbind(intent) }
    override fun onDestroy() { cleanup(); super.onDestroy() }
}
