# BUILD_VERIFICATION v0.5.01

Basis: v0.5.00 / 141. Neuer Quellstand: v0.5.01 / 142. Keine Android-APK gebaut.

| Prüfung | Ergebnis |
|---|---|
| `python verify_v0501_analyzer_controls.py` | PASS 20/20 (statisch) |
| `python verify_v0500_analyzer_layout.py` | PASS 20/20 (statisch, Altvertrag angepasst) |
| Kotlin-Host Waterfall | PASS 6/6 |
| Kotlin-Host DisplayRange/Average | PASS 7/7 |
| `:app:testDebugUnitTest` | NOT RUN (Android SDK fehlt) |
| `assembleDebug` | NOT RUN (Android SDK fehlt) |
| Xiaomi v0.5.01 | NOT RUN |
| Supabase Restore | NOT RUN |

Näheres: `docs/project/ANALYZER_CONTROLS_v0.5.01.md` und `PROJECT_STATUS.md`. Statische Checks ersetzen keinen Compose-Build.
