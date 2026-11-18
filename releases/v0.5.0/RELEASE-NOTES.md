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

## VirusTotal report

[View the scan for this exact APK](https://www.virustotal.com/gui/file/2b866654a05901a542038fcabb595778124dab772bffbd720f3ca76ca3e44c11/detection). Checked November 18, 2026: **0 / 67 vendors flagged the file as malicious**; last analysis shown November 16, 2026 at 11:54:50 PKT (UTC+05:00). The APK SHA-256 is `2b866654a05901a542038fcabb595778124dab772bffbd720f3ca76ca3e44c11`.

A clean scan is not a safety guarantee or Play Protect approval. Android may still block Internet-sideloaded apps requesting Accessibility access. A review appeal has been submitted; its outcome is unconfirmed. Keep device protections enabled if installation is blocked. See [Security and verification](../../SECURITY.md) for permissions, checksum and certificate details.
