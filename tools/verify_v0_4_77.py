from pathlib import Path
root=Path(__file__).resolve().parents[1]
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text()
exp=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text()
policy=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text()
gradle=(root/'app/build.gradle.kts').read_text()
branch=main.index('if (data.sourceKind == "HLNUG_WBBU_IMAGE" && data.sourceImageBytes != null)')
save=main.index('Text("Plot speichern")')
checks={
 'version': 'versionName = "0.4.77"' in gradle and 'versionCode = 117' in gradle,
 'save_button_before_plot_branch': save < branch,
 'common_save_feedback': 'Plot erfolgreich gespeichert' in main and 'SEISMIC_PLOT_SAVE_OK' in main,
 'near_live_continuous_path': 'drawPath(' in main and 'SeismicScheliPolicy' in main,
 'near_export_continuous_path': 'drawPath(' in exp and 'SeismicScheliPolicy' in exp,
 'scheli_48x1800': 'SCHELI_ROWS = 48' in policy and 'SCHELI_ROW_SECONDS = 1800' in policy,
 'same_zoom_controls': 'wbbu_zoom' in main and 'near_zoom' in main,
 'storage_preserved': 'Download/LFN-Scout/' in exp,
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL'),k)
raise SystemExit(1 if not all(checks.values()) else 0)
