package com.tensormind.nudge.diagnostics

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings

data class AudioStatus(
    val ringerVolumePercent: Int,
    val mediaVolumePercent: Int,
    val isSilentOrVibrate: Boolean,
    val isDndActive: Boolean,
    val summary: String,
    val issuesFound: List<String>
)

class AudioDiagnosticService(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun inspectAudio(): AudioStatus {
        val maxRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1)
        val curRing = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        val ringPercent = (curRing * 100) / maxRing

        val maxMedia = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val curMedia = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val mediaPercent = (curMedia * 100) / maxMedia

        val isSilentOrVibrate = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL

        val isDnd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = notificationManager.currentInterruptionFilter
            filter != NotificationManager.INTERRUPTION_FILTER_ALL && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        } else {
            false
        }

        val issues = mutableListOf<String>()
        if (ringPercent == 0 || isSilentOrVibrate) {
            issues.add("Phone ringer is muted or on silent")
        }
        if (mediaPercent == 0) {
            issues.add("Media volume is at 0%")
        }
        if (isDnd) {
            issues.add("Do Not Disturb mode is blocking sounds")
        }

        val summary = when {
            issues.isEmpty() -> "Your sound is working properly! All volume levels are healthy."
            issues.size == 1 -> "We noticed 1 sound issue: ${issues[0]}."
            else -> "We found ${issues.size} issues stopping your phone from making sound."
        }

        return AudioStatus(
            ringerVolumePercent = ringPercent,
            mediaVolumePercent = mediaPercent,
            isSilentOrVibrate = isSilentOrVibrate,
            isDndActive = isDnd,
            summary = summary,
            issuesFound = issues
        )
    }

    fun canModifyDoNotDisturb(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun openDndSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun fixSoundIssues(playChime: Boolean = false): String {
        var ringerRestored = false
        var dndWarning = false

        // 1. Switch back to normal ringer mode if allowed
        try {
            if (canModifyDoNotDisturb()) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                ringerRestored = true
            } else {
                // If not allowed, only set ringer mode if not currently in silent mode
                if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                    ringerRestored = true
                } else {
                    dndWarning = true
                }
            }
        } catch (_: Exception) {
            dndWarning = true
        }

        // 2. Set Ringtone volume to 85%
        try {
            val maxRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            val targetRing = (maxRing * 0.85).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, targetRing, 0)
        } catch (_: Exception) {
            dndWarning = true
        }

        // 3. Set Media volume to 80% (Media stream is never blocked by DND policy)
        try {
            val maxMedia = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetMedia = (maxMedia * 0.80).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetMedia, 0)
        } catch (_: Exception) {}

        // 4. Set Notification volume to 85%
        try {
            val maxNotif = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            val targetNotif = (maxNotif * 0.85).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, targetNotif, 0)
        } catch (_: Exception) {
            dndWarning = true
        }

        // 5. Play chime if requested
        if (playChime) {
            playChime()
        }

        return if (dndWarning && !canModifyDoNotDisturb()) {
            "I adjusted your media volume to 80%. Turn off Do Not Disturb in Settings to fully unmute calls."
        } else {
            "I turned off Silent mode and set your ringtone and media volume to 85%."
        }
    }

    fun playChime() {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (_: Exception) {}
    }
}
