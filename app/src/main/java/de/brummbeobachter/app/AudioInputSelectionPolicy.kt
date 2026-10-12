package de.brummbeobachter.app

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

enum class AudioSourceMode {
    AUTO,
    UNPROCESSED,
    MIC;

    companion object {
        fun fromStored(value: String?): AudioSourceMode =
            entries.firstOrNull { it.name == value } ?: AUTO
    }

    fun displayName(): String =
        when (this) {
            AUTO -> "Auto · UNPROCESSED bevorzugt"
            UNPROCESSED -> "UNPROCESSED"
            MIC -> "MIC"
        }
}

data class AudioInputOption(
    val deviceId: Int,
    val label: String
)

object AudioInputSelectionPolicy {
    const val AUTO_DEVICE_ID = -1

    fun sourceOrder(mode: AudioSourceMode): List<AudioSourceMode> =
        when (mode) {
            AudioSourceMode.AUTO ->
                listOf(
                    AudioSourceMode.UNPROCESSED,
                    AudioSourceMode.MIC
                )
            AudioSourceMode.UNPROCESSED ->
                listOf(AudioSourceMode.UNPROCESSED)
            AudioSourceMode.MIC ->
                listOf(AudioSourceMode.MIC)
        }

    fun normalizeDeviceId(
        selectedDeviceId: Int,
        availableDeviceIds: Set<Int>
    ): Int =
        if (
            selectedDeviceId == AUTO_DEVICE_ID ||
            selectedDeviceId in availableDeviceIds
        ) {
            selectedDeviceId
        } else {
            AUTO_DEVICE_ID
        }
}

object AudioInputManager {

    fun listInputs(context: Context): List<AudioInputOption> {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return audioManager
            .getDevices(AudioManager.GET_DEVICES_INPUTS)
            .map { device ->
                AudioInputOption(
                    deviceId = device.id,
                    label = buildLabel(device)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun findInput(
        context: Context,
        deviceId: Int
    ): AudioDeviceInfo? {
        if (deviceId == AudioInputSelectionPolicy.AUTO_DEVICE_ID) {
            return null
        }

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return audioManager
            .getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.id == deviceId }
    }

    fun buildLabel(device: AudioDeviceInfo): String {
        val product =
            device.productName
                ?.toString()
                ?.trim()
                .orEmpty()

        val type =
            when (device.type) {
                AudioDeviceInfo.TYPE_BUILTIN_MIC ->
                    "Internes Mikrofon"
                AudioDeviceInfo.TYPE_WIRED_HEADSET ->
                    "Headset-Mikrofon"
                AudioDeviceInfo.TYPE_USB_DEVICE ->
                    "USB-Audio"
                AudioDeviceInfo.TYPE_USB_HEADSET ->
                    "USB-Headset"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO ->
                    "Bluetooth-SCO"
                AudioDeviceInfo.TYPE_TELEPHONY ->
                    "Telefonie"
                AudioDeviceInfo.TYPE_LINE_ANALOG ->
                    "Line-In analog"
                AudioDeviceInfo.TYPE_LINE_DIGITAL ->
                    "Line-In digital"
                else ->
                    "Audioeingang Typ ${device.type}"
            }

        return if (
            product.isNotBlank() &&
            !product.equals(type, ignoreCase = true)
        ) {
            "$type · $product"
        } else {
            type
        }
    }
}
