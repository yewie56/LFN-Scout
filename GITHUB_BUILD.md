# LFN-Scout v0.4.62 – Build mit GitHub Actions

Der Workflow liegt unter `.github/workflows/build-android.yml` und heißt **Build LFN-Scout**.

## Ablauf

1. Push oder manueller Start (`workflow_dispatch`).
2. Java 17 wird eingerichtet.
3. Gradle wird vorbereitet.
4. Die App-Version wird automatisch aus `app/build.gradle.kts` gelesen.
5. `./gradlew testDebugUnitTest --stacktrace` läuft.
6. Nur bei erfolgreichen Tests folgt `./gradlew assembleDebug --stacktrace`.
7. Die APK wird versionsbezogen in `LFN-Scout-vX.Y.Z-debug.apk` umbenannt.
8. Die APK wird weiterhin als GitHub-Artifact hochgeladen.
9. Bei der derzeit aktiven **Variante B** (`STORE_APK_IN_REPOSITORY: "true"`) wird die APK zusätzlich unter `Builds/debug/` committed und nach `main` gepusht.
10. Der automatische APK-Commit trägt `[skip ci]`; zusätzlich löst ein Push mit dem `GITHUB_TOKEN` keinen normalen rekursiven Workflow-Lauf aus.

## Variante B (aktuell)

```yaml
env:
  STORE_APK_IN_REPOSITORY: "true"
```

Ergebnis zusätzlich im Repository:

```text
Builds/debug/LFN-Scout-vX.Y.Z-debug.apk
```

## Variante A (später möglich)

Zum Abschalten der APK-Ablage im Git-Repository:

```yaml
STORE_APK_IN_REPOSITORY: "false"
```

Das GitHub-Artifact bleibt trotzdem erhalten.

## Windows-Updater

Die genaue Bedienung steht in `UPDATER_ANLEITUNG.md` bzw. `Tools/WindowsUpdater/README.md`.
