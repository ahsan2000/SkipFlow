# Security and download verification

## VirusTotal scan: v0.5.0

| Item | Recorded value |
| --- | --- |
| File | `SkipFlow-v0.5.0.apk` |
| Package | `com.ahsan.skipflow` |
| VirusTotal result | **0 / 67 vendors flagged the file as malicious** |
| Report checked | November 18, 2026 |
| Last analysis shown | November 16, 2026, 11:54:50 PKT (UTC+05:00) |
| APK SHA-256 | `2b866654a05901a542038fcabb595778124dab772bffbd720f3ca76ca3e44c11` |

[Inspect the public VirusTotal report](https://www.virustotal.com/gui/file/2b866654a05901a542038fcabb595778124dab772bffbd720f3ca76ca3e44c11/detection). Some engines cannot process this file type; the table records the report's detection ratio, not a certification from every engine. Results can change after reanalysis. This page documents the observed report; it does not alter the VirusTotal result.

A clean scan cannot establish that software is risk-free. It also does not establish Google Play policy compliance or Play Protect approval. The result above applies only to the exact file hash listed.

## Obtain and check the APK

Download from the [official release](https://github.com/ahsan2000/SkipFlow/releases/tag/v0.5.0). Avoid modified APKs or mirrors whose hashes differ. Compare the downloaded APK against [SHA256SUMS](releases/v0.5.0/SHA256SUMS):

```sh
# Place SHA256SUMS beside SkipFlow-v0.5.0.apk, then run in that folder:
sha256sum -c SHA256SUMS
```

On Windows PowerShell:

```powershell
Get-FileHash .\SkipFlow-v0.5.0.apk -Algorithm SHA256
```

The hash must match the table above. If it differs, do not install that file. These checks establish artifact identity, not the absence of vulnerabilities.

### Check the signing certificate

With Android SDK Build Tools available:

```sh
apksigner verify --verbose --print-certs SkipFlow-v0.5.0.apk
```

The signature should verify, and the signer certificate SHA-256 should match:

```text
d69238dabee5e9127e5d926f0610a9fa16b69279abb2065e9bdbd2d26255e475
```

The release verification record is in [SIGNATURE.txt](releases/v0.5.0/SIGNATURE.txt). This APK uses v2 and v3 signatures. Signing identifies the key used to sign the APK; it is not a security audit. Locally built APKs may have different hashes and signing keys and are not covered by this scan.

## Access and local data

- **Accessibility:** manually enabled by the user. The service can retrieve visible window content and is configured for the YouTube and YouTube Music packages. It uses their visible controls to detect ads and request a click on an available Skip ad button. Screen contents are not saved or uploaded. Review [the service configuration](composeApp/src/androidMain/res/xml/skip_service.xml) before granting access.
- **Audio settings:** `MODIFY_AUDIO_SETTINGS` allows the optional media-volume feature to lower detected ads to level 1 and restore the saved level. It does not record audio. Manual changes take priority.
- **Floating controls:** use an Accessibility overlay. No separate `SYSTEM_ALERT_WINDOW` permission is requested.
- **Local preferences:** include feature switches, skip-click count, floating position and temporary volume-recovery state. Uninstalling removes this app data. Immediate volume restoration cannot be guaranteed while Android has killed the service.
- **Network and personal data:** the app does not request Internet, SMS, contacts, microphone or location permissions, or notification-listener access. Opening the portfolio launches an external browser, which has its own privacy behavior.
- **Release build:** not debuggable; the development stall-probe receiver is excluded. AndroidX may contribute its standard app-local receiver permission and profile installer component; these are separate from the app's Accessibility capability.

Review the [manifest](composeApp/src/androidMain/AndroidManifest.xml) and [source](composeApp/src/androidMain) for implementation details.

## Play Protect status

A user reported this installation block: “This app can request access to sensitive data.” Google documents a corresponding anti-fraud restriction for certain Internet-sideloaded apps with sensitive capabilities, including Accessibility, in its [warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance).

The warning and a VirusTotal scan measure different things. Even the Google engine's “Undetected” entry in VirusTotal is not evidence that Play Protect will permit installation. A review appeal was submitted; its outcome is unconfirmed as of November 18, 2026. The app has not been published on Google Play. Keep device protections enabled if installation is blocked.

## Reporting a concern

For a non-sensitive bug or security concern, [open a GitHub issue](https://github.com/ahsan2000/SkipFlow/issues) with the app version, Android version and a description. Do not post credentials, private screen contents or other sensitive information in a public issue.

## Future releases

Each new APK needs its own checksum, signature verification and scan link. Do not reuse this version's result to describe another build.
