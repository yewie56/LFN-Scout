# v0.5.14 – Build-Verifikation

Android/Compose-Gradle: NOT RUN. Xiaomi: NOT RUN. Nur statische Quellenprüfungen vorgesehen. Auf Windows zuerst PREPARE_PROJECT.bat, danach `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`.

Lokale statische Checks: `python tools/verify_unified_v0514.py` – PASS 12/12 (Schlüsselvertrag, UI-Verfügbarkeit, Version, Dokumente; kein Kotlin-Typecheck). Kotlin-PSI-Syntax der zwei veränderten Kotlin-Dateien: PASS 2/2. Diese Prüfungen ersetzen weder Android-Compile noch Laufzeitverifikation.
