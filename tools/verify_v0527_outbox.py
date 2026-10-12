"""Static regression checks; does NOT replace Android compilation or device tests."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/'app/src/main/java/de/brummbeobachter/app'
out=(java/'AudioStatisticsOutbox.kt').read_text()
cloud=(java/'AudioStatisticsCloud.kt').read_text()
main=(java/'MainActivity.kt').read_text()
repo=(java/'LocalAudioStatisticsRepository.kt').read_text()
checks={
    'secret removed before persistence': 'request.remove("key")' in out,
    'staged and ready states': '".ready"' in out and 'f.extension == "ready"' in out,
    'set identity stable': all(word in out for word in ('recording_id','set_kind','set_index','lookup_version')),
    'atomic temp then rename': 'temp.renameTo(destination)' in out,
    'failed requests retained': 'if (client.submitAudioStatistics(request))' in out,
    'observer sidecar exports analysis': 'analysisJson = json.toString()' in repo,
    'observer queues before upload': 'queueObserver(appContext, archiveEntry, analysis)' in main and 'saveObserverSidecar(' in main,
    'observer ready after WAV success': 'markRecordingUploaded(appContext, archived.recordingId)' in main,
    'analyzer enqueues': 'AudioStatisticsOutbox.enqueue(context, requests)' in cloud,
    'analyzer ready after WAV success': 'markRecordingUploaded(context, entry.recordingId)' in cloud,
    'manual retry drains': 'AUDIO_STATISTICS_RETRY' in main,
    'existing API preserved': 'suspend fun submitAudioStatistics(payload: JSONObject)' in (java/'SupabaseClient.kt').read_text(),
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL')+': '+name)
assert all(checks.values()), 'Static outbox regression failed'
print(f'PASS: {len(checks)}/{len(checks)} static checks (Android compilation not performed)')
