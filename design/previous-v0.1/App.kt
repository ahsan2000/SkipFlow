package com.ahsan.skipflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun App(enabled: Boolean, connected: Boolean, youtube: Boolean, music: Boolean, count: Int, onToggle: (Boolean)->Unit, onYoutube: (Boolean)->Unit, onMusic: (Boolean)->Unit, onSetup: ()->Unit) {
 MaterialTheme(colorScheme = darkColorScheme(primary=Color(0xFF76E2C1), background=Color(0xFF0B101A), surface=Color(0xFF151E2D))) {
  Surface(Modifier.fillMaxSize()) {
   Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement=Arrangement.spacedBy(20.dp)) {
    Text("SKIPFLOW", style=MaterialTheme.typography.labelLarge, color=MaterialTheme.colorScheme.primary)
    Text("Less reaching. More listening.", style=MaterialTheme.typography.headlineLarge)
    Text("Automatically tap Skip ad when it becomes available.", color=Color(0xFFA8B6C9))
    Card { Column(Modifier.padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
     Text(if (!connected) "Setup needed" else if (enabled) "Ready to skip" else "Auto skip paused", style=MaterialTheme.typography.titleLarge)
     Text(if (connected) "Accessibility service is enabled." else "Enable the service once in Android settings.")
     Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("Auto skip", Modifier.weight(1f).padding(top=12.dp)); Switch(enabled, onToggle) }
     Button(onClick=onSetup, modifier=Modifier.fillMaxWidth()) { Text(if (connected) "Accessibility settings" else "Set up auto skip") }
    } }
    Text("YOUR APPS", style=MaterialTheme.typography.labelLarge, color=MaterialTheme.colorScheme.primary)
    Card { Column(Modifier.padding(16.dp)) {
     Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("YouTube", Modifier.weight(1f).padding(top=12.dp)); Switch(youtube, onYoutube) }
     Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("YouTube Music", Modifier.weight(1f).padding(top=12.dp)); Switch(music, onMusic) }
    } }
    Text("$count successful skip clicks", style=MaterialTheme.typography.titleMedium)
    Text("Keep the screen on and the player visible. Unskippable ads play normally. English and Urdu ad labels are supported; app updates may affect detection.", color=Color(0xFFA8B6C9))
    HorizontalDivider()
    Text("Private by design", style=MaterialTheme.typography.titleMedium)
    Text("Visible controls are processed on your phone. No recordings, accounts, or internet permission. Set up while parked before driving.", color=Color(0xFFA8B6C9))
   }
  }
 }
}
