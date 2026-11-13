# SkipFlow v0.5.0

Direct-download Android release. Requires Android 8.0 or newer.

## Features
- Automatically tap an available Skip ad button in YouTube and YouTube Music.
- Optionally lower detected ad volume to media level 1 and restore the saved level afterward.
- Floating Pause/Resume and Home controls, edge docking and fading.
- Drag the icon onto the cross to hide it; enable Floating controls in the app to restore it.
- Optional skip-count celebration and a preview that does not change real statistics.
- Floating controls use an independent UI thread so Accessibility screen-query waits do not block their gestures.

## Setup
1. Install SkipFlow-v0.5.0.apk.
2. Open SkipFlow, read its Accessibility disclosure, and follow its setup screen to enable the service manually.
3. Enable the desired YouTube apps and resume auto skip. Keep the screen on and the player visible.

Unskippable ads keep playing. Detection depends on visible Accessibility controls; YouTube updates and different device layouts can affect it. The feature set has been tested on a Samsung SM-A175F; compatibility on every Android device has not been established.

## Release verification
The APK is release-signed and not debuggable. The development stall-probe receiver and its class are excluded. Signature and ZIP alignment checks passed. The bundled Lato font license is included at assets/licenses/Lato-OFL.txt. See SHA256SUMS for the download checksum and SIGNATURE.txt for public certificate information.

## Updating the earlier test build
This release uses a new private release certificate. It cannot replace an installed debug-signed copy directly. Removing the old app deletes its local settings and skip count; plan a backup/migration before doing so. This release task did not uninstall or change the copy on the developer's Samsung.

Screen controls are processed locally; the app does not request Internet or microphone permission. The portfolio link opens an external browser. SkipFlow is an independent app and is not affiliated with Google or YouTube.
