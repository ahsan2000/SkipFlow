package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle=SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),navigationBarStyle=SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val prefs = getSharedPreferences("skipflow", MODE_PRIVATE)
        val brandFont = FontFamily(Font(R.font.lato_regular,FontWeight.Normal),Font(R.font.lato_bold,FontWeight.Bold))
        setContent {
            var enabled by remember { mutableStateOf(prefs.getBoolean("enabled", false)) }
            var youtube by remember { mutableStateOf(prefs.getBoolean(SkipPolicy.YOUTUBE, true)) }
            var music by remember { mutableStateOf(prefs.getBoolean(SkipPolicy.MUSIC, true)) }
            var lowerAdVolume by remember { mutableStateOf(prefs.getBoolean("lower_ad_volume",true)) }
            var volumeActive by remember { mutableStateOf(prefs.getBoolean(AdVolumeController.ACTIVE,false)) }
            var floatingControls by remember { mutableStateOf(prefs.getBoolean("floating_controls",true)) }
            var skipAnimation by remember { mutableStateOf(prefs.getBoolean("skip_animation",true)) }
            var connected by remember { mutableStateOf(false) }
            var count by remember { mutableIntStateOf(prefs.getInt("count",0)) }
            var disclosure by rememberSaveable { mutableStateOf(false) }
            DisposableEffect(Unit) {
                fun refresh() {
                    val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
                    connected = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any { it.resolveInfo.serviceInfo.packageName == packageName }
                    enabled = prefs.getBoolean("enabled",false)
                    youtube = prefs.getBoolean(SkipPolicy.YOUTUBE,true)
                    music = prefs.getBoolean(SkipPolicy.MUSIC,true)
                    count = prefs.getInt("count",0)
                    floatingControls = prefs.getBoolean("floating_controls",true)
                    skipAnimation = prefs.getBoolean("skip_animation",true)
                    lowerAdVolume = prefs.getBoolean("lower_ad_volume",true)
                    volumeActive = prefs.getBoolean(AdVolumeController.ACTIVE,false)
                }
                val accessibility = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
                val stateListener = AccessibilityManager.AccessibilityStateChangeListener { refresh() }
                accessibility.addAccessibilityStateChangeListener(stateListener)
                val servicesListener = if (Build.VERSION.SDK_INT >= 33) {
                    AccessibilityManager.AccessibilityServicesStateChangeListener { refresh() }.also {
                        accessibility.addAccessibilityServicesStateChangeListener(it)
                    }
                } else null
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _,_ -> refresh() }
                val observer = LifecycleEventObserver { _,event -> if(event == Lifecycle.Event.ON_RESUME) refresh() }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                lifecycle.addObserver(observer)
                refresh()
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                    lifecycle.removeObserver(observer)
                    accessibility.removeAccessibilityStateChangeListener(stateListener)
                    if (Build.VERSION.SDK_INT >= 33 && servicesListener != null) accessibility.removeAccessibilityServicesStateChangeListener(servicesListener)
                }
            }
            SkipFlowTheme(brandFont) {
                App(enabled,connected,youtube,music,count,
                    lowerAdVolume=lowerAdVolume,volumeActive=volumeActive,
                    floatingControls=floatingControls,skipAnimation=skipAnimation,
                    onFloatingControls={prefs.edit().putBoolean("floating_controls",it).apply()},
                    onSkipAnimation={prefs.edit().putBoolean("skip_animation",it).apply()},
                    onPreviewAnimation={
                        if (connected) prefs.edit().putLong("animation_preview",System.nanoTime()).apply()
                        else Toast.makeText(this,"Enable SkipFlow Accessibility access to preview the animation.",Toast.LENGTH_SHORT).show()
                    },
                    onLowerAdVolume={prefs.edit().putBoolean("lower_ad_volume",it).apply()},
                    onToggle={prefs.edit().putBoolean("enabled",it).apply()},
                    onYoutube={prefs.edit().putBoolean(SkipPolicy.YOUTUBE,it).apply()},
                    onMusic={prefs.edit().putBoolean(SkipPolicy.MUSIC,it).apply()},
                    onSetup={if (connected) startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) else disclosure=true},
                    onPortfolio={
                        try { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://ahsan2000.github.io/portfolio/"))) }
                        catch (_: ActivityNotFoundException) { Toast.makeText(this,"Install a browser to open the portfolio.",Toast.LENGTH_SHORT).show() }
                    },
                    onOpenApp={pkg ->
                        val launch = packageManager.getLaunchIntentForPackage(pkg)
                        if (launch != null) startActivity(launch)
                        else Toast.makeText(this,"This app is not installed in your current profile.",Toast.LENGTH_SHORT).show()
                    }
                )
                if (disclosure) AlertDialog(
                    onDismissRequest={disclosure=false},
                    title={Text("You're in control")},
                    text={Text("SkipFlow uses Accessibility to read visible controls in YouTube and YouTube Music and tap available Skip ad buttons for you. When Lower ad volume is enabled, it also lowers media volume to 1 during detected ads and restores your previous volume afterward. It also displays floating pause/resume and Home controls, plus a brief animation after successful skip clicks. Both can be turned off in SkipFlow. Screen content stays on your phone and is never saved or transmitted.\n\nOn the next screen, select Installed apps → SkipFlow auto skip and enable access. You can revoke it at any time.")},
                    confirmButton={TextButton(onClick={
                        disclosure=false
                        if (!connected) prefs.edit().putBoolean("enabled",true).apply()
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }) { Text("Agree & open settings") }},
                    dismissButton={TextButton(onClick={disclosure=false}) { Text("Cancel") }}
                )
            }
        }
    }
}
