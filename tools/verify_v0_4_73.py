from pathlib import Path
root=Path(__file__).resolve().parents[1]
policy=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text()
exp=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text()
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text()
gradle=(root/'app/build.gradle.kts').read_text()
checks={
 'version': 'versionName = "0.4.73"' in gradle and 'versionCode = 113' in gradle,
 'target': 'EXPORT_WIDTH_PX = 1536' in policy and 'EXPORT_HEIGHT_PX = 864' in policy,
 'supersample': 'EXPORT_SUPERSAMPLE = 2' in policy,
 'hires_bitmap': 'EXPORT_WIDTH_PX * SeismicPlotUiPolicy.EXPORT_SUPERSAMPLE' in exp,
 'downsample': 'Bitmap.createScaledBitmap' in exp,
 'legend_value_unit': '40000 nm/s' in exp,
 'legend_before_times': 'legendWidth' in exp and 'timeLabelWidth' in exp,
 'ui_vertical_label': 'rotationZ = -90f' in main and '40000 nm/s' in main,
 'old_storage_preserved': 'Download/LFN-Scout/' in exp,
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL'),k)
failed=[k for k,v in checks.items() if not v]
raise SystemExit(1 if failed else 0)
