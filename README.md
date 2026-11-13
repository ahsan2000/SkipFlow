# SkipFlow
Kotlin Multiplatform prototype: shared Compose UI and skip policy, native Android AccessibilityService. A desktop JVM target runs shared policy tests; desktop automation and iOS are not implemented.

## Build
Use JDK 21 and Android SDK 36. Set sdk.dir in local.properties.
```
./gradlew :composeApp:assembleDebug :composeApp:desktopTest
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```
Open SkipFlow, read the disclosure, enable its service in Android Accessibility settings, and switch Auto skip on. Enable access manually; do not grant it via shell commands.

Supports visible YouTube / YouTube Music ad controls by known IDs or exact English/Urdu ad-skip labels. Never clicks generic Next/Skip controls. No network permission or screen-content persistence. Screen-off, locked-screen and unskippable ads are outside scope. Counters record successful accessibility click requests, not proof that playback resumed.

## Device validation
Test each app separately: skippable ad, countdown, unskippable ad, pause switch, app switches, portrait/landscape, lock screen, and next-track controls. Real ad skipping remains unverified until observed on device. Do not use UIAutomator concurrently when verifying accessibility behavior, as it can interfere with accessibility services.

Google Play distribution needs policy review and is not guaranteed. No affiliation with YouTube or Google.

## Initial verification — 2026-11-11
- Debug APK assembled and installed on Samsung SM-A175F (Android 17).
- Launch succeeded; screen visually inspected; no AndroidRuntime crash output.
- Shared policy test passed, including rejection of generic Skip, Next, countdown labels, and unrelated packages.
- Android lint completed with 0 errors and 13 warnings (dependency versions, target API, compatibility and style suggestions).
- User manually enabled Accessibility. ADB confirmed SkipService bound and the master setting enabled.
- User subsequently confirmed successful automatic ad skipping in YouTube Music on the Samsung; the stored click count reached 1. Main YouTube ad skipping remains unverified.


## Design refresh — v0.2.0
- Navy/mint dashboard with bundled licensed Lato fonts, clear pause/resume states, app shortcuts, a scrollable guide, and a locally stored skip-click counter.
- Built by Ahsan Nawaz credit opens https://ahsan2000.github.io/portfolio/ in the browser; verified via the launched Chrome intent.
- Original editable vector assets: design/skipflow-mark.svg and design/skipflow-icon.svg. Matching adaptive and Android 13+ themed launcher icons; dark launch screen.
- Lifecycle, preference and accessibility-state listeners replace the UI refresh timer. Ad detection and native service logic are unchanged.
- Debug APK assembled; shared policy test passed; Android lint completed with zero errors and nonblocking warnings.
- Dashboard, guide, fonts, scrolling and portfolio footer visually checked on the attached Samsung. Existing master preference and count were preserved by in-place updates.
- After repeated APK updates Samsung showed the Accessibility service enabled but waiting to bind. If the dashboard shows setup needed, toggle SkipFlow off and back on in Android Accessibility settings. Access must be managed manually by the user.
- The logo was drawn for this project; exclusivity is unverified, and a preliminary search found other uses of the SkipFlow name. See design/BRAND.md.

Final verification: after the final update, ADB confirmed SkipService bound, no pending binding, and no crashed service or AndroidRuntime crash output.


## Ad volume control — v0.3.0 (2026-11-13)
- New **Lower ad volume** switch defaults to on, following the existing master and per-app switches.
- Uses Android STREAM_MUSIC only. A detected visible ad saves the current media level once and lowers it to 1; already-muted audio and level 1 are preserved. Unskippable ads can be lowered too when visible ad markers are exposed.
- Restoration follows 1.5 seconds of stable absence of ad markers, including after an automatic skip. Short gaps between consecutive ads do not overwrite the original snapshot.
- Pause, feature disable, leaving a selected app, screen lock/off and service teardown restore the saved level. Inaccessible windows have a 3-second grace period to avoid reacting to transient overlays.
- Manual volume changes take priority for that ad break. Switching audio output abandons that break's snapshot rather than applying one output's volume to another. Fixed-volume outputs and call audio are not controlled.
- Saves the volume snapshot before lowering. A later service connection recovers it only when the output still matches and media volume is still 1. Force-killing a process cannot guarantee immediate restoration while the service is unavailable.
- Generic Ad/Sponsored badges are accepted only within a known player subtree. Titles, recommendations and unsupported apps are excluded. Ads whose controls are not exposed through Accessibility may not be detected. Screen-on native playback is the supported scope; cast and background playback are not verified.
- MODIFY_AUDIO_SETTINGS is a normal Android permission; no microphone, notification-policy or Internet permission was added.

### Verification
- Debug app and Android test APK assembled.
- Seven shared tests passed (six new ad-signal/volume-session tests and the existing skip policy test).
- Android lint: 0 errors, 18 nonblocking warnings.
- Samsung SM-A175F native volume test passed: actual media level reduced to 1, restored after synthetic ad-end and stop signals, manual changes respected, persisted snapshot recovered. Test always restores the starting media level and plays no audio.
- The native device test injects ad-state signals into the production volume adapter. The user subsequently confirmed the volume feature works during real ads on the Samsung.
- Instrumentation restarts the target process and can disconnect Accessibility; manually toggle the service off and on after testing.

Device test command (debug only):
```
./gradlew :composeApp:assembleDebugAndroidTest
adb install -r composeApp/build/outputs/apk/androidTest/debug/composeApp-debug-androidTest.apk
adb shell am instrument -w com.ahsan.skipflow.test/com.ahsan.skipflow.VolumeInstrumentation
```


## Floating controls and skip animation — v0.4.0 (2026-11-13)
- Draggable SkipFlow Accessibility overlay, enabled by default, with snap-to-edge positioning stored as normalized coordinates for orientation changes.
- Tap to expand **Pause / Resume** and **Home**. Pausing uses the existing master setting, stopping both skip and ad-volume automation and restoring any saved volume. The service stays bound so the bubble can resume automation. Home opens MainActivity.
- The icon remains available over other apps while Accessibility is connected and the phone is unlocked. Hide it using the Floating controls switch. No SYSTEM_ALERT_WINDOW permission was added.
- After each successful skip click, a small touch-through overlay animates **1 + previous count = new total**, then fades away. Sequential receipts queue rather than overwrite one another. Screen lock, disabling animations, configuration changes and service teardown remove the animation.
- A separate Skip celebration switch controls real-skip receipts. The explicitly labelled preview uses the current count but never writes a fake skip or increments statistics. Android's disabled-animation setting is respected.
- Existing dashboard now includes the latest skip equation; an in-app guide explains the controls.

### Device validation
- Debug APK built and installed on Samsung SM-A175F. Seven shared tests passed. Lint completed with zero errors and nonblocking compatibility/style warnings.
- Verified overlay attachment and visibility using WindowManager, and visually inspected floating controls and equation preview.
- Verified dragging and saved edge coordinates.
- Verified Pause writes enabled=false, Resume writes enabled=true, and both leave count unchanged (25 before and after).
- Verified floating Home opens SkipFlow from Samsung Launcher. The initial test exposed a detached-view Handler callback error; qualified the owning controller's Handler, rebuilt, and repeated Home and pause/resume successfully. No new AndroidRuntime errors were logged by the fixed process.
- Verified preview displays **1 + 25 = 26** while the actual saved count remains **25**. The overlay is NOT_TOUCHABLE and NOT_FOCUSABLE.
- Actual automatic triggering on the next real ad uses the production successful-click path; the recorded visual check used the labelled preview rather than generating a fake skip.
- Left the service bound and master setting enabled. Original per-app and volume preferences were preserved.


## Quiet floating icon — v0.4.1
- Main skip statistic shows only the total with its label; the last-skip equation was removed from that card. The equation remains in the animation settings and skip receipt.
- After 3 seconds of collapsed inactivity, the bubble slides into the nearest edge and fades to 55% opacity. Only half of the icon remains visible and touchable, using a clipped half-width window rather than an invisible full-width touch target.
- A tap slides the bubble fully out, restores full opacity, and opens Pause / Resume and Home. Dragging restores full visibility. Menu auto-collapse, lock/unlock, orientation changes and disabled system animations are handled.
- Verified on Samsung SM-A175F: main count 25 with no equation; idle floating window 79 pixels wide, menu 575 pixels wide after tapping, then redocked to 79 pixels. Visually checked opacity and clipping. No new AndroidRuntime errors for the updated process.
- Debug build and all 7 shared tests passed; Android lint reported 0 errors and nonblocking warnings.

## Drag directly from the docked handle — v0.4.2
- Reproduced failure: a continuous swipe starting on the faded half left the saved position unchanged.
- The half-docked handle overlaps Android's Back gesture zone. On Android 10+, its visible host bounds now use systemGestureExclusionRects so dragging the handle takes priority. The exclusion covers only the icon's current visible viewport, not the rest of the screen edge.
- Built and installed on the attached Samsung SM-A175F; Android lint passed with zero errors.
- Verified uninterrupted right-to-left and left-to-right drags beginning on the 79-pixel-wide faded handle, without a preliminary tap. Saved horizontal position changed 1.0 → 0.0 → 1.0. Count stayed 25.
- Verified an explicit 800 ms press-and-hold followed by movement from the faded half. The drag reached the opposite edge without first expanding the menu. Returned the handle to the right edge afterward; no AndroidRuntime errors in the new process.


## Independent overlay UI and drag-to-close — v0.5.0 (2026-11-13)
- Floating controls, drag targets, timers, drawing and skip receipts now run on a dedicated SkipFlowOverlay Looper. All overlay View operations stay on their owning thread; the Accessibility service's blocking screen-tree queries cannot queue ahead of pointer input on that thread.
- A worker-owned visibility guard handles lock state and the Floating controls preference independently of Accessibility tree queries. Pause-state and volume settings are revalidated after a potentially slow query before applying its result.
- During a drag, a touch-through bottom cross target appears. It highlights and changes to Release to hide when the icon overlaps it. Releasing there sets floating_controls=false and immediately removes both overlays. Cancellation and non-target releases keep floating controls enabled.
- Dropping on the cross changes only the Floating controls setting. The master pause state, selected apps, ad-volume option, skip animation option and real skip count are preserved. Re-enable Floating controls inside SkipFlow to restore the icon at its last saved edge position.
- Added an updated guide and floating-controls description.

### Verification
- Debug APK built; all 7 shared tests passed; Android lint: 0 errors, 27 nonblocking warnings.
- Regression probe: explicitly blocked the application's main thread for 2 seconds. A complete drag began and ended on SkipFlowOverlay before that block ended, and the actual window moved while main was blocked. Maximum sampled input-event delay was 36 ms; count remained 25. This is a controlled probe, not a guarantee of every real-world frame time.
- Probe receiver exists only in androidDebug and is protected by android.permission.DUMP (ADB/system access); release source sets do not include it.
- Real device drag-to-close test passed: hovered target was visually checked, drop wrote floating_controls=false, both overlays disappeared, and the icon stayed hidden. The existing master setting was paused before and after; count stayed 25.
- Re-enabled via the actual app switch; the floating window returned without changing the master setting. No AndroidRuntime errors were reported by the updated process during these checks.
- Evidence: design/verification/v0.5/*.json and design/screenshots/drag-to-hide-v05.png.
- A final pause-race safeguard was rebuilt and installed afterward; final service reconnection/device check follows below.


Final v0.5.0 verification after the pause-race safeguard and Accessibility reconnection:
- Repeated the protected 2-second main-thread stall. A complete drag still finished on SkipFlowOverlay before MAIN_STALL_END; worst sampled input-event lag was 27 ms and count remained 25.
- Pause/Resume both passed on the dedicated overlay thread, restoring the starting master state (enabled=true). Home opened MainActivity from Samsung Launcher. No AndroidRuntime errors in the final process.
- Floating controls were re-enabled through the real in-app switch after drag-to-close testing; the icon is available again. The Accessibility service reconnected successfully after the user toggled it off/on.
- Latest metrics are in design/verification/v0.5/stall-result.json and controls-result.json. The drag-to-close and re-enable evidence was captured on the same overlay implementation before the final pause-state-only safeguard.


## Direct APK release
Signed v0.5.0 APK and checksum are available under releases/v0.5.0/. See RELEASING.md for GitHub upload and future signing instructions. The private .signing/ directory must never be published.
