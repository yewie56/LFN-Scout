package de.brummbeobachter.app

/**
 * Phase 3O/11: conservative, READ-ONLY microphone ownership diagnostic.
 * This is NOT a synchronization primitive: recorder transitions do not share a
 * transaction lock. Never interpret an idle observation as permission to apply.
 */
internal object AnalyzerSetupAudioOwnerGate {
    data class Observation(
        val microphoneOwner: String?,
        val rollingRunning: Boolean,
        val directCapturing: Boolean,
        val monRecording: Boolean
    ) {
        val blockers: List<String> get() = buildList {
            if (microphoneOwner != null) add("Mikrofon belegt: $microphoneOwner")
            if (rollingRunning) add("Rolling-Audiostream aktiv")
            if (directCapturing) add("Direct-Audio-Service aktiv")
            if (monRecording) add("MON-Aufnahme aktiv")
        }
        val apparentlyIdle: Boolean get() = blockers.isEmpty()
    }

    fun observe(): Observation = Observation(
        microphoneOwner = MicCaptureArbiter.owner,
        rollingRunning = RollingAudioEngine.isRunning,
        directCapturing = AnalyzerDirectAudioService.isCapturing,
        monRecording = AnalyzerMonitorRecording.isActive()
    )

}
