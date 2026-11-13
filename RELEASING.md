# Building and sharing SkipFlow releases

## Current download
Public assets are in releases/v0.5.0/:
- SkipFlow-v0.5.0.apk
- SHA256SUMS
- SIGNATURE.txt
- RELEASE-NOTES.md

For GitHub: create a release tagged v0.5.0, paste RELEASE-NOTES.md into its description, and attach the APK and checksum file. SIGNATURE.txt may also be attached. No GitHub upload was performed by the assistant.

## Private signing material
.signing/ contains the private release keystore and its password file. Keep that folder private and securely backed up outside the repository. It is Git-ignored, as are common keystore extensions. Never attach it to a GitHub release. Future official APK updates must use this same key; losing it prevents updating installations signed with it.

The certificate is different from the old development/debug certificate, so the first release APK cannot update that earlier debug installation in place. Uninstalling deletes local settings/counts; arrange a backup or migration first.

## Rebuild
Use JDK 21 and Android SDK build-tools 36.0.0. Set ANDROID_HOME or sdk.dir in local.properties. Restore the private signing folder locally, then run:

```bash
JAVA_HOME=/path/to/jdk-21 python3 scripts/package_release.py
```

The script builds release and lint, aligns and signs the APK without printing passwords, rejects debuggable/test-probe packages, verifies the signature/alignment and required font license, and creates the checksum and public signature report. Increment versionCode and versionName in composeApp/build.gradle.kts for subsequent releases.

The deliberate main-thread stall probe lives only in androidDebug. It is not included in this release. Successful packaging does not establish Google Play policy eligibility; review the relevant Play policies separately.


## What belongs in Git
Commit composeApp/ source and tests, gradle/ (including gradle-wrapper.jar), gradlew, Gradle configuration, scripts/, .gitignore, documentation, logo SVGs, licenses and useful screenshots. Release notes, SHA256SUMS and SIGNATURE.txt are public metadata and may be committed.

Do not commit .signing/, keystores/passwords, local.properties, IDE/build caches, generated APK/AAB/IDSIG files, obsolete design/previous-v0.1/ backups, device verification JSON, or the internal Play review. .gitignore now covers these. Ignored files previously tracked were removed from the index; their local copies remain available. Do not force-add them.

## Push the source
This checkout already has a Git repository, a main branch and an origin remote. No git init or remote add is needed here.

```bash
cd /data/mobile_apps/skipflow
git add .
git status
git diff --cached --stat
git commit -m "Prepare SkipFlow v0.5.0 release"
git push origin main
```

Review the staged file list before committing. Private signing files and the APK should not be listed. The assistant updated ignore rules and the index, but did not commit or push changes.

## Create the GitHub Release in your browser
1. Open your GitHub repository and select Releases, then Draft a new release (or Create a new release for the first one).
2. In the tag selector, enter v0.5.0 and create that tag, targeting main after the source push.
3. Set the title to SkipFlow v0.5.0.
4. Paste releases/v0.5.0/RELEASE-NOTES.md into the description.
5. Attach the local releases/v0.5.0/SkipFlow-v0.5.0.apk and SHA256SUMS files. SIGNATURE.txt is optional and public. These are release attachments, not source commits.
6. For this first beta, mark it as a pre-release. Review the files, then click Publish release.
7. Open the published release and confirm the APK appears under Assets. Share the release page URL so people can download it.

Official instructions: https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository
