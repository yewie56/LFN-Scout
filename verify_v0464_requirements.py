from pathlib import Path
import sys

root = Path(__file__).resolve().parent
checks = []

def check(name, condition):
    checks.append((name, bool(condition)))
    print(("PASS" if condition else "FAIL") + ": " + name)

build = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
service = (root / "app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt").read_text(encoding="utf-8")
policy = (root / "app/src/main/java/de/brummbeobachter/app/SeismicStationPolicy.kt").read_text(encoding="utf-8")
wave = (root / "app/src/main/java/de/brummbeobachter/app/SeismicWaveformPolicy.kt").read_text(encoding="utf-8")
main = (root / "app/src/main/java/de/brummbeobachter/app/MainActivity.kt").read_text(encoding="utf-8")
changelog = (root / "CHANGELOG.md").read_text(encoding="utf-8")
readme = (root / "README.md").read_text(encoding="utf-8")
project_doc = (root / "docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md").read_text(encoding="utf-8")

a = lambda s: s in service
check("versionName 0.4.64", 'versionName = "0.4.64"' in build)
check("versionCode 104", "versionCode = 104" in build)
check("old static GEOFON PNG endpoint removed", "waveform/data/" not in service and "LIVE_IMAGE_BASE" not in service)
check("FDSN station endpoint present", "fdsnws/station/1/query" in service)
check("FDSN availability endpoint present", "fdsnws/availability/1/query" in service)
check("FDSN dataselect endpoint present", "fdsnws/dataselect/1/query" in service)
check("vertical channel query present", '"channel" to "??Z"' in service)
check("HHZ/BHZ channel preference present", '"HHZ" -> 0' in policy and '"BHZ" -> 1' in policy)
check("MiniSEED integer decoding present", "1 -> IntArray" in wave and "3 -> IntArray" in wave)
check("MiniSEED Steim1 decoding present", "10 -> decodeSteim" in wave)
check("MiniSEED Steim2 decoding present", "11 -> decodeSteim" in wave)
check("native Compose Canvas plot present", "Canvas(" in main and "data.envelope.bins" in main)
check("60 minute plot label present", 'Text("−60 min"' in main)
check("no seismic BitmapFactory decode remains", "data.imageBytes" not in main and "imageLastModifiedEpochMs" not in main)
check("availability logging present", a("SEISMIC_AVAILABILITY_OK"))
check("no-current-data logging present", a("SEISMIC_NO_CURRENT_DATA"))
check("MiniSEED parse-error logging present", a("SEISMIC_MINISEED_PARSE_ERROR"))
check("plot-success logging present", a("SEISMIC_PLOT_OK"))
check("waveform-success logging present", a("SEISMIC_WAVEFORM_OK"))
check("selftest present", (root / "SeismicWaveformSelfTest.kt").exists())
check("JUnit regression test present", (root / "app/src/test/java/de/brummbeobachter/app/SeismicWaveformPolicyTest.kt").exists())
check("CHANGELOG documents 0.4.64", "## 0.4.64" in changelog and "fdsnws-availability" in changelog)
check("README documents 0.4.64", "# LFN-Scout Android v0.4.64" in readme)
check("project documentation updated", "Änderungen in v0.4.64" in project_doc)
check("official project docs still present", all((root / "docs/project" / name).exists() for name in ["README.md", "LFN-Scout_Aenderungen_seit_v0.4.58.md", "LFN-Scout_Geplante_Erweiterungen.md"]))
check("mandatory root docs still present", all((root / name).exists() for name in ["BUILD_WORKFLOWS.md", "DIRECTIVES.md", "CHANGELOG.md"]))

passed = sum(ok for _, ok in checks)
print(f"\nRESULT: {passed}/{len(checks)} PASS")
if passed != len(checks):
    sys.exit(1)
