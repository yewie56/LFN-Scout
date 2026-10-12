package de.brummbeobachter.app

enum class SoundSelfTestResult(
    val displayText: String
) {
    STARTED(
        "Selbsttest: PASS – Ton wird abgespielt"
    ),
    STOPPED(
        "Selbsttest: Ton gestoppt"
    ),
    FAILED(
        "Selbsttest: FAIL – Ton konnte nicht abgespielt werden"
    )
}

object SoundSelfTestEvents {
    const val STARTED = "SOUND_SELFTEST_STARTED"
    const val STOPPED = "SOUND_SELFTEST_STOPPED"
    const val FAILED = "SOUND_SELFTEST_FAILED"
}
