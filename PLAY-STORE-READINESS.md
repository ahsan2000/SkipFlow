# SkipFlow: Google Play readiness review
Reviewed 2026-11-13. Current project version: 0.5.0, applicationId com.ahsan.skipflow.

## Decision
Not ready for a production submission. The current app is a working device-tested prototype, but policy eligibility and release packaging remain unresolved. No Play Console upload or submission has been made.

## Main policy concern
Google Play's Device and Network Abuse policy prohibits interfering with advertising displayed by another app. SkipFlow explicitly detects ads in YouTube and YouTube Music, clicks enabled Skip controls, and optionally lowers volume specifically during ads. This creates a serious rejection risk. This is an assessment of the policy's likely application, not a Google determination about SkipFlow.

The Accessibility policy permits narrow deterministic automation, but explicitly requires compliance with other Play policies. Correct Accessibility disclosure alone does not resolve the advertising-policy concern. Keep isAccessibilityTool=false: this app was designed as general convenience automation, not primarily as a tool for users with disabilities. Do not misrepresent its purpose or conceal features.

Recommended next step: request policy guidance through Play Console Help with an accurate demonstration of all features. Support guidance is not a guarantee of later approval. Decide whether to pursue this current feature set or redesign before investing in submission assets and signing setup.

Sources:
- https://support.google.com/googleplay/android-developer/answer/16559646
- https://support.google.com/googleplay/android-developer/answer/10964491

## Verified technical status
- compileSdk/targetSdk=36, minSdk=26. The current published requirement for new standard Android apps is API 36 or newer, so this target matches that requirement.
- Existing development workflow builds and installs a debug APK. The reviewed Gradle configuration has no explicit release/upload signing configuration. A signed release Android App Bundle is still needed for a new Play app.
- The stall probe receiver is in androidDebug and its manifest is mapped only to the debug variant. A release audit must confirm neither the probe receiver nor debug-only classes are packaged, and that the release is not debuggable.
- In-app Accessibility disclosure exists and isAccessibilityTool=false. A Play Console Accessibility declaration and demonstration video still need preparation and review against the final app behavior.
- The app has local settings, skip counts and temporary volume recovery state. Its portfolio link opens a separate browser. Data safety answers must describe the final release and its dependencies accurately; do not confuse local processing of screen controls with off-device collection.
- No public privacy-policy link was found in the reviewed app UI/source. Google requires a publicly accessible policy and a link inside the app and store listing, including for apps declaring no data collection.
- Store listing text, store-specific graphics/screenshots, support contact, content rating, target audience, app access and Data safety forms still need completion.
- SkipFlow has other known uses as a name. The original project logo is not proof of exclusive branding rights; naming remains an unresolved launch decision.
- Developer account existence/type, account verification, production access, upload-key ownership and signing-key arrangements are not established in this chat.
- Personal developer accounts created after 2023-11-13 generally require a closed test with at least 12 continuously opted-in testers for 14 days before applying for production access. Account-specific requirements must be checked in Console.

Release references:
- https://support.google.com/googleplay/android-developer/answer/11926878
- https://developer.android.com/guide/app-bundle
- https://support.google.com/googleplay/android-developer/answer/9859455
- https://support.google.com/googleplay/android-developer/answer/14151465

## Draft policy inquiry — not sent
Hello Google Play Developer Support,

I am preparing an Android app called SkipFlow and would like guidance on the Device and Network Abuse and AccessibilityService policies before submission.

The app uses a user-enabled AccessibilityService, limited to the official YouTube and YouTube Music packages. It automatically clicks an existing Skip ad button only after the app makes that button available. It does not skip unskippable ads early, modify either app, block network requests, or alter ad delivery. An optional feature detects visible ad controls, saves the media-volume level, lowers it to level 1 during the detected ad, and restores the earlier level afterward. Floating controls let users pause/resume automation or hide the overlay. Processing is local; the app does not transmit screen contents.

Does either the automatic Skip-button interaction or the ad-specific volume feature fall under the prohibition on interfering with another app displaying ads? What changes, if any, would be required for policy compliance? I can provide a complete demonstration video and the required Accessibility declaration.

Thank you.
