# Persistent system test v0.5.77

## Real live-test scope

The Diagnostics button now executes three *real* Analyzer STOP/RUN transitions, with a 33s listening period for PCM heartbeats, **only** when the Analyzer is RUN in `ROLLING_8K` and Rolling was already active in Scout before entering the Analyzer. Read-only checks compare real Rolling ownership and MON state/configuration.

## Safety

The controller never starts, stops, resets or reconfigures Scout Rolling. It never changes named user JSON setups, Cloud queue or user recordings. If preconditions fail all live transition checks report SKIP. On any error it attempts to restore Analyzer RUN and records FAIL. Do not navigate away during a test.

## Output

Current-run-tagged results are persisted in app-private `systemtest` storage. ZIP export uses the last completed JSON and CSV verbatim. App log is included for diagnosis.

## Limitations

This is not yet a complete unattended suite: genuine safe setup A↔B and filterbank changes require isolated temporary fixture management and rollback; no block-perfect PCM capture or disk WAV write-continuity measurement is claimed. Sample monotonicity still relies on heartbeat observations. These cases remain SKIP. Android Gradle build and on-device test remain outstanding.
