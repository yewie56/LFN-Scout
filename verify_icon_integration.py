
from pathlib import Path
import sys

root = Path(__file__).resolve().parent
res = root / "app/src/main/res"
manifest = root / "app/src/main/AndroidManifest.xml"
build = root / "app/build.gradle.kts"

required = [
    res / "drawable/ic_launcher_foreground.xml",
    res / "drawable/ic_launcher_monochrome.xml",
    res / "mipmap-anydpi-v26/ic_launcher.xml",
    res / "mipmap-anydpi-v26/ic_launcher_round.xml",
]
for density in ["mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"]:
    required += [
        res / f"mipmap-{density}/ic_launcher.png",
        res / f"mipmap-{density}/ic_launcher_round.png",
    ]

missing = [str(p.relative_to(root)) for p in required if not p.exists()]
if missing:
    raise SystemExit("Missing icon resources: " + ", ".join(missing))

colors = (res / "values/colors.xml").read_text(encoding="utf-8")
if '<color name="ic_launcher_background">#082A52</color>' not in colors:
    raise SystemExit("Launcher background color missing")

m = manifest.read_text(encoding="utf-8")
if 'android:icon="@mipmap/ic_launcher"' not in m:
    raise SystemExit("android:icon missing")
if 'android:roundIcon="@mipmap/ic_launcher_round"' not in m:
    raise SystemExit("android:roundIcon missing")

b = build.read_text(encoding="utf-8")
if 'versionName = "0.4.41"' not in b or 'versionCode = 81' not in b:
    raise SystemExit("Version 0.4.41 / code 81 missing")

print("icon integration verification PASS")
