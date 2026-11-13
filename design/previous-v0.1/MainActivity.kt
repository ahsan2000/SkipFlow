package com.ahsan.skipflow

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs = getSharedPreferences("skipflow", MODE_PRIVATE)
  setContent {
   var enabled by remember { mutableStateOf(prefs.getBoolean("enabled", false)) }
   var youtube by remember { mutableStateOf(prefs.getBoolean(SkipPolicy.YOUTUBE, true)) }
   var music by remember { mutableStateOf(prefs.getBoolean(SkipPolicy.MUSIC, true)) }
   var connected by remember { mutableStateOf(false) }
   var count by remember { mutableIntStateOf(0) }
   var disclosure by remember { mutableStateOf(false) }
   LaunchedEffect(Unit) {
    while (true) {
     val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
     connected = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any { it.resolveInfo.serviceInfo.packageName == packageName }
     count = prefs.getInt("count", 0)
     delay(1000)
    }
   }
   App(enabled, connected, youtube, music, count,
    { enabled=it; prefs.edit().putBoolean("enabled",it).apply() },
    { youtube=it; prefs.edit().putBoolean(SkipPolicy.YOUTUBE,it).apply() },
    { music=it; prefs.edit().putBoolean(SkipPolicy.MUSIC,it).apply() },
    { disclosure=true })
   if (disclosure) androidx.compose.material3.AlertDialog(
    onDismissRequest={disclosure=false},
    title={ androidx.compose.material3.Text("Allow automatic skip clicks") },
    text={ androidx.compose.material3.Text("SkipFlow uses Accessibility to read visible controls in YouTube and YouTube Music and click available Skip ad buttons for you. Screen content is processed locally and is never stored or transmitted. You can revoke access in Android settings at any time. On the next screen, select SkipFlow and enable it.") },
    confirmButton={ androidx.compose.material3.TextButton(onClick={ disclosure=false; startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { androidx.compose.material3.Text("Agree & open settings") } },
    dismissButton={ androidx.compose.material3.TextButton(onClick={disclosure=false}) { androidx.compose.material3.Text("Cancel") } }
   )
  }
 }
}
