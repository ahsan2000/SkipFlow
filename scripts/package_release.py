#!/usr/bin/env python3
"""Build, align, sign and verify a GitHub-ready APK. Private keys are never copied."""
from pathlib import Path
import argparse, hashlib, os, re, subprocess, tempfile, zipfile

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--skip-build', action='store_true', help='Package the already-built release APK')
args = parser.parse_args()
private = root / '.signing'
key = private / 'skipflow-release.p12'
password = private / 'release-password.txt'
if not key.is_file() or not password.is_file():
    raise SystemExit('Missing private release key/password in .signing/. Restore your secure backup; do not replace the key used by existing releases.')
sdk = os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT')
if not sdk:
    for line in (root / 'local.properties').read_text().splitlines():
        if line.startswith('sdk.dir='): sdk = line.split('=', 1)[1].replace('\\:', ':').replace('\\\\', '\\')
if not sdk: raise SystemExit('Set ANDROID_HOME to your Android SDK.')
build_tools = Path(sdk) / 'build-tools' / '36.0.0'

def run(argv):
    result = subprocess.run([str(x) for x in argv], cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode: raise SystemExit(result.stdout)
    return result.stdout

if not args.skip_build:
    print('Building release APK and running Android lint (use JDK 21).', flush=True)
    run([root / 'gradlew', ':composeApp:assembleRelease', ':composeApp:lintRelease', '--console=plain'])
source = root / 'composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk'
if not source.is_file(): raise SystemExit('Release APK not found; run assembleRelease first.')
badging = run([build_tools / 'aapt', 'dump', 'badging', source])
manifest = run([build_tools / 'aapt', 'dump', 'xmltree', source, 'AndroidManifest.xml'])
version = re.search(r"versionName='([A-Za-z0-9_.-]+)'", badging).group(1)
if 'application-debuggable' in badging or 'android:debuggable(0x0101000f)=(type 0x12)0xffffffff' in manifest:
    raise SystemExit('Refusing to distribute a debuggable APK.')
if 'android:testOnly' in manifest or 'DebugUiStallReceiver' in manifest or 'E: instrumentation ' in manifest:
    raise SystemExit('Refusing APK containing debug/test components.')
with zipfile.ZipFile(source) as apk:
    if any(b'DebugUiStallReceiver' in apk.read(name) for name in apk.namelist() if name.endswith('.dex')):
        raise SystemExit('Debug stall-probe class unexpectedly packaged in release.')
out = root / 'releases' / ('v' + version)
out.mkdir(parents=True, exist_ok=True)
final = out / ('SkipFlow-v' + version + '.apk')
with tempfile.TemporaryDirectory(prefix='skipflow-sign-') as work:
    aligned = Path(work) / 'aligned.apk'
    run([build_tools / 'zipalign', '-P', '16', '-f', '4', source, aligned])
    run([build_tools / 'apksigner', 'sign', '--ks', key, '--ks-key-alias', 'skipflow-release', '--ks-pass', 'file:' + str(password), '--key-pass', 'file:' + str(password), '--v4-signing-enabled', 'false', '--out', final, aligned])
verification = run([build_tools / 'apksigner', 'verify', '--verbose', '--print-certs', final])
run([build_tools / 'zipalign', '-c', '-P', '16', '4', final])
digest = hashlib.sha256(final.read_bytes()).hexdigest()
(out / 'SHA256SUMS').write_text(digest + '  ' + final.name + '\n')
(out / 'SIGNATURE.txt').write_text(verification)
print('APK: ' + str(final))
print('Size: %.2f MiB' % (final.stat().st_size / 1024 / 1024))
print('SHA256: ' + digest)
print('Verified release manifest, debug-probe exclusion, signature and ZIP alignment.')
