package com.ahsan.skipflow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF0A1019)
private val Panel = Color(0xFF141E2B)
private val Mint = Color(0xFF78E4C4)
private val Muted = Color(0xFF9AABBF)
private val Line = Color(0xFF283546)
private val White = Color(0xFFF2F6FC)

@Composable
fun SkipFlowTheme(fontFamily: FontFamily = FontFamily.SansSerif, content: @Composable () -> Unit) {
    val base = Typography()
    fun TextStyle.brand() = copy(fontFamily = fontFamily)
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Mint, onPrimary = Ink, primaryContainer = Color(0xFF183C35),
            onPrimaryContainer = Mint, background = Ink, onBackground = White,
            surface = Panel, onSurface = White, surfaceVariant = Color(0xFF1D2A3B),
            onSurfaceVariant = Muted, outline = Line, secondary = Color(0xFFAAC9FF),
            onSecondaryContainer = White, secondaryContainer = Color(0xFF23344D)
        ),
        typography = Typography(
            displayLarge = base.displayLarge.brand(), displayMedium = base.displayMedium.brand(),
            displaySmall = base.displaySmall.brand(), headlineLarge = base.headlineLarge.brand(),
            headlineMedium = base.headlineMedium.brand(), headlineSmall = base.headlineSmall.brand(),
            titleLarge = base.titleLarge.brand(), titleMedium = base.titleMedium.brand(),
            titleSmall = base.titleSmall.brand(), bodyLarge = base.bodyLarge.brand(),
            bodyMedium = base.bodyMedium.brand(), bodySmall = base.bodySmall.brand(),
            labelLarge = base.labelLarge.brand(), labelMedium = base.labelMedium.brand(),
            labelSmall = base.labelSmall.brand()
        ), content = content
    )
}

/** Original vector mark: two offset flow ribbons forming an S and a forward cut. */
@Composable
fun FlowMark(modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = "SkipFlow logo" }) {
        val factor = size.minDimension / 100f
        scale(factor, factor, pivot = Offset.Zero) {
            val upper = Path().apply {
                moveTo(73f, 19f); lineTo(39f, 19f)
                cubicTo(15f, 19f, 12f, 46f, 34f, 53f)
                lineTo(51f, 58f); lineTo(58f, 45f); lineTo(38f, 39f)
                cubicTo(32f, 37f, 33f, 32f, 40f, 32f)
                lineTo(65f, 32f); lineTo(65f, 39f); lineTo(84f, 25f); lineTo(65f, 11f)
                lineTo(65f, 19f); close()
            }
            val lower = Path().apply {
                moveTo(27f, 81f); lineTo(61f, 81f)
                cubicTo(85f, 81f, 88f, 54f, 66f, 47f)
                lineTo(61f, 45f); lineTo(54f, 58f); lineTo(62f, 61f)
                cubicTo(68f, 63f, 67f, 68f, 60f, 68f)
                lineTo(35f, 68f); lineTo(35f, 61f); lineTo(16f, 75f); lineTo(35f, 89f)
                lineTo(35f, 81f); close()
            }
            drawPath(upper, Mint)
            drawPath(lower, Color(0xFF87ABF9))
        }
    }
}

@Composable
private fun Glyph(kind: String, modifier: Modifier = Modifier, tint: Color = Mint) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val stroke = Stroke(width = w * .075f, cap = StrokeCap.Round)
        when (kind) {
            "play" -> drawPath(Path().apply { moveTo(w*.38f,h*.24f); lineTo(w*.76f,h*.5f); lineTo(w*.38f,h*.76f); close() }, tint)
            "music" -> {
                drawCircle(tint, w*.38f, Offset(w*.5f,h*.5f), style=stroke)
                drawPath(Path().apply { moveTo(w*.43f,h*.31f); lineTo(w*.68f,h*.5f); lineTo(w*.43f,h*.69f); close() }, tint)
            }
            "arrow" -> {
                drawLine(tint, Offset(w*.28f,h*.72f), Offset(w*.72f,h*.28f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w*.3f,h*.28f), Offset(w*.72f,h*.28f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w*.72f,h*.28f), Offset(w*.72f,h*.7f), stroke.width, StrokeCap.Round)
            }
            "shield" -> {
                drawPath(Path().apply { moveTo(w*.5f,h*.12f); lineTo(w*.81f,h*.25f); lineTo(w*.77f,h*.6f); quadraticTo(w*.7f,h*.8f,w*.5f,h*.9f); quadraticTo(w*.3f,h*.8f,w*.23f,h*.6f); lineTo(w*.19f,h*.25f); close() }, tint, style=stroke)
                drawLine(tint,Offset(w*.35f,h*.5f),Offset(w*.46f,h*.62f),stroke.width,StrokeCap.Round)
                drawLine(tint,Offset(w*.46f,h*.62f),Offset(w*.66f,h*.39f),stroke.width,StrokeCap.Round)
            }
            "pause" -> {
                drawLine(tint,Offset(w*.36f,h*.25f),Offset(w*.36f,h*.75f),w*.12f,StrokeCap.Round)
                drawLine(tint,Offset(w*.64f,h*.25f),Offset(w*.64f,h*.75f),w*.12f,StrokeCap.Round)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    enabled: Boolean, connected: Boolean, youtube: Boolean, music: Boolean, count: Int,
    onToggle: (Boolean)->Unit, onYoutube: (Boolean)->Unit, onMusic: (Boolean)->Unit,
    lowerAdVolume: Boolean, volumeActive: Boolean, onLowerAdVolume: (Boolean)->Unit,
    floatingControls: Boolean, skipAnimation: Boolean, onFloatingControls: (Boolean)->Unit,
    onSkipAnimation: (Boolean)->Unit, onPreviewAnimation: ()->Unit,
    onSetup: ()->Unit, onPortfolio: ()->Unit, onOpenApp: (String)->Unit
) {
    var showGuide by remember { mutableStateOf(false) }
    val active = enabled && connected
    val accent by animateColorAsState(if (active) Mint else if (connected) Color(0xFFAAC9FF) else Color(0xFFF2CB8B))
    Surface(Modifier.fillMaxSize(), color = Ink) {
        Column(Modifier.safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal=24.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(13.dp)), contentAlignment=Alignment.Center) { FlowMark(Modifier.size(34.dp)) }
                Spacer(Modifier.width(10.dp))
                Text("SkipFlow", fontSize=21.sp, fontWeight=FontWeight.Bold, letterSpacing=(-.5).sp, modifier=Modifier.weight(1f))
                TextButton(onClick={showGuide=true}) { Text("Guide", color=Muted) }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(top=16.dp,bottom=24.dp), verticalArrangement=Arrangement.spacedBy(22.dp)) {
                Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Text("Stay in your flow.", fontSize=29.sp, lineHeight=35.sp, letterSpacing=(-.8).sp, fontWeight=FontWeight.Bold, modifier=Modifier.semantics { heading() })
                    Text("Your music. Fewer interruptions.", fontSize=14.sp, color=Muted)
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF1B302F),Panel))).border(1.dp,Color(0xFF2C4545),RoundedCornerShape(28.dp)).animateContentSize().padding(22.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(78.dp).clip(CircleShape).background(accent.copy(alpha=.08f)).border(1.dp,accent.copy(alpha=.25f),CircleShape),contentAlignment=Alignment.Center) {
                        if (active) FlowMark(Modifier.size(60.dp)) else Glyph(if (connected) "pause" else "shield",Modifier.size(34.dp),accent)
                    }
                    Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
                        Text(if (active) "AUTO SKIP IS ON" else if (connected) "AUTO SKIP IS PAUSED" else "ONE STEP TO GET STARTED",color=accent,fontSize=10.sp,letterSpacing=1.3.sp,fontWeight=FontWeight.Bold)
                    }
                    Text(if (active) "You're all set." else if (connected) "Take a pause." else "Make yourself hands-free.",fontSize=24.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.6).sp)
                    Text(if (!connected) "Enable access to tap available Skip ad buttons for you." else if (enabled && !youtube && !music) "Choose an app below to start automatic skipping." else if (enabled) "We'll tap Skip ad as soon as it's available." else "Turn auto skip back on whenever you're ready.",color=Muted,fontSize=13.sp,lineHeight=19.sp)
                    Button(onClick={if (!connected) onSetup() else onToggle(!enabled)},modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=if(active) Color(0xFF263F3D) else Mint,contentColor=if(active) Mint else Ink)) {
                        if (connected) { Glyph(if (enabled) "pause" else "play",Modifier.size(18.dp),if(active) Mint else Ink); Spacer(Modifier.width(8.dp)) }
                        Text(if (!connected) "Enable auto skip" else if (enabled) "Pause auto skip" else "Resume auto skip",fontWeight=FontWeight.Bold)
                    }
                    if (!connected) Text("One-time setup · You stay in control",color=Muted,fontSize=11.sp)
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(20.dp)).padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Mint.copy(alpha=.08f)),contentAlignment=Alignment.Center) { Glyph("play",Modifier.size(25.dp)) }
                    Spacer(Modifier.width(14.dp))
                    AnimatedContent(targetState=count,label="Skip counter") { value -> Text(value.toString(),fontSize=28.sp,fontWeight=FontWeight.Bold,letterSpacing=(-1).sp) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text("Skip clicks",fontWeight=FontWeight.Bold,fontSize=13.sp); Text("Since you started",fontSize=11.sp,color=Muted) }
                    Glyph("shield",Modifier.size(22.dp),Muted)
                }
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("Connected apps",fontSize=16.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f).semantics { heading() })
                        Text("${(if(youtube) 1 else 0)+(if(music) 1 else 0)} selected",fontSize=11.sp,color=Muted)
                    }
                    Column(Modifier.clip(RoundedCornerShape(22.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(22.dp))) {
                        AppRow("YouTube","Videos, without the extra tap",false,youtube,onYoutube) { onOpenApp(SkipPolicy.YOUTUBE) }
                        HorizontalDivider(Modifier.padding(horizontal=18.dp),color=Line)
                        AppRow("YouTube Music","Keep your listening flowing",true,music,onMusic) { onOpenApp(SkipPolicy.MUSIC) }
                    }
                    Text("Keep the screen on and the player visible. Ads without a Skip button keep playing; their volume can still be lowered when detected.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(22.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end=12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                            Text("Lower ad volume",fontSize=15.sp,fontWeight=FontWeight.Bold)
                            Text("Level 1 during ads. Your volume after.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                        }
                        Switch(checked=lowerAdVolume,onCheckedChange=onLowerAdVolume,modifier=Modifier.semantics { contentDescription="Lower ad volume" },colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Mint,uncheckedThumbColor=Muted,uncheckedTrackColor=Color(0xFF273446),uncheckedBorderColor=Color.Transparent))
                    }
                    if (volumeActive) Text("Ad detected · Media volume lowered to 1",fontSize=12.sp,color=Mint)
                    Text("Restores your saved media volume when the ad ends or you leave the app. Manual volume changes are respected. Works while auto skip and the selected app are enabled.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).border(1.dp,Line,RoundedCornerShape(22.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    Text("Always within reach",fontSize=16.sp,fontWeight=FontWeight.Bold)
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end=12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Text("Floating controls",fontSize=14.sp,fontWeight=FontWeight.Bold)
                            Text("Tap for Pause / Resume and Home. Drag to move, or drop on × to hide.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                        }
                        Switch(checked=floatingControls,onCheckedChange=onFloatingControls,modifier=Modifier.semantics { contentDescription="Floating controls" },colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Mint,uncheckedThumbColor=Muted,uncheckedTrackColor=Color(0xFF273446),uncheckedBorderColor=Color.Transparent))
                    }
                    HorizontalDivider(color=Line)
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end=12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                            Text("Skip celebration",fontSize=14.sp,fontWeight=FontWeight.Bold)
                            Text("A little +1 every time SkipFlow skips an ad.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                        }
                        Switch(checked=skipAnimation,onCheckedChange=onSkipAnimation,modifier=Modifier.semantics { contentDescription="Skip celebration" },colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Mint,uncheckedThumbColor=Muted,uncheckedTrackColor=Color(0xFF273446),uncheckedBorderColor=Color.Transparent))
                    }
                    Text("1 + $count = ${count.toLong()+1}",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Mint)
                    TextButton(onClick=onPreviewAnimation,modifier=Modifier.fillMaxWidth()) { Text("Preview animation · doesn't change your count",fontSize=11.sp) }
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF102520)).padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Glyph("shield",Modifier.size(22.dp))
                    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Text("On your phone. Under your control.",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Mint)
                        Text("No accounts, recordings or uploaded screen data.",fontSize=12.sp,lineHeight=18.sp,color=Muted)
                    }
                }
                if (connected) OutlinedButton(onClick=onSetup,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)) { Text("Manage accessibility access",fontSize=12.sp) }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick=onPortfolio).padding(vertical=14.dp,horizontal=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF253249)),contentAlignment=Alignment.Center) { Text("AN",fontWeight=FontWeight.Bold,fontSize=12.sp,color=Color(0xFFAAC9FF)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) { Text("Built by Ahsan Nawaz",fontSize=13.sp,fontWeight=FontWeight.Bold); Text("Explore my portfolio",fontSize=11.sp,color=Muted) }
                    Glyph("arrow",Modifier.size(20.dp),Muted)
                }
                Text("SKIPFLOW  /  0.5.0",fontSize=9.sp,letterSpacing=1.7.sp,color=Muted.copy(alpha=.65f),modifier=Modifier.align(Alignment.CenterHorizontally))
            }
        }
        if (showGuide) ModalBottomSheet(onDismissRequest={showGuide=false},containerColor=Panel) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Text("A little setup. A better flow.",fontSize=23.sp,fontWeight=FontWeight.Bold)
                GuideStep("01","Enable access","Allow SkipFlow in Android Accessibility settings. You can revoke access at any time.")
                GuideStep("02","Choose your apps","Turn on YouTube, YouTube Music, or both. Tap an app name to open it.")
                GuideStep("03","Press play","Keep the screen on. When a Skip ad button becomes available, SkipFlow taps it for you.")
                GuideStep("04","Stay in control","Drag the floating SkipFlow icon anywhere. Tap it for Pause / Resume or Home to open this app. The animation shows 1 + previous count = new total after each successful skip click. Drop the icon on the close target to hide it; enable Floating controls here to bring it back. This hides only the icon. Pausing also restores ad volume; the floating button remains available to resume.")
                HorizontalDivider(color=Line)
                Text("A few things to know",fontWeight=FontWeight.Bold)
                Text("Lower ad volume reduces detected ads to media level 1, then restores your previous level. Already-muted audio stays muted. If you change volume during an ad, your choice takes priority. Switching audio outputs cancels the saved level for that break. Unskippable ads keep playing. English and Urdu ad labels are supported; app updates may affect detection. Set up while parked before driving.",color=Muted,fontSize=13.sp,lineHeight=20.sp)
                Text("Privacy",fontWeight=FontWeight.Bold)
                Text("Accessibility reads visible controls in the two selected YouTube apps. Screen contents are processed locally and never saved or transmitted. Only your settings, click count and temporary volume recovery state are stored. Opening the portfolio uses your browser.",color=Muted,fontSize=13.sp,lineHeight=20.sp)
                Button(onClick={showGuide=false},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text("Got it") }
            }
        }
    }
}

@Composable
private fun AppRow(name: String, subtitle: String, music: Boolean, checked: Boolean, onToggle: (Boolean)->Unit, onOpen: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if(music) Color(0xFF382333) else Color(0xFF382A29)),contentAlignment=Alignment.Center) { Glyph(if(music) "music" else "play",Modifier.size(28.dp),Color(0xFFFFA9A0)) }
        Row(Modifier.weight(1f).padding(start=12.dp,end=4.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick=onOpen).heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) { Text(name,fontSize=14.sp,fontWeight=FontWeight.Bold); Text(subtitle,fontSize=10.sp,lineHeight=14.sp,color=Muted) }
            Glyph("arrow",Modifier.size(15.dp),Muted)
        }
        Switch(checked=checked,onCheckedChange=onToggle,modifier=Modifier.semantics { contentDescription="Auto skip in $name" },colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Mint,uncheckedThumbColor=Muted,uncheckedTrackColor=Color(0xFF273446),uncheckedBorderColor=Color.Transparent))
    }
}

@Composable
private fun GuideStep(number: String, title: String, detail: String) {
    Row(horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(number,color=Mint,fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=3.dp))
        Column(verticalArrangement=Arrangement.spacedBy(5.dp)) { Text(title,fontWeight=FontWeight.Bold,fontSize=15.sp); Text(detail,color=Muted,fontSize=13.sp,lineHeight=20.sp) }
    }
}
