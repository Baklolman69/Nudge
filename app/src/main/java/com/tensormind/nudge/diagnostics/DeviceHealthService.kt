package com.tensormind.nudge.diagnostics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs

data class DeviceHealthReport(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val soundHealthy: Boolean,
    val networkHealthy: Boolean,
    val overallScore: String
)

class DeviceHealthService(private val context: Context) {

    private val audioService = AudioDiagnosticService(context)
    private val networkService = NetworkDiagnosticService(context)

    fun runFullCheckup(): DeviceHealthReport {
        // Battery
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPercent = (level * 100) / scale.coerceAtLeast(1)

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
        val bytesTotal = stat.blockCountLong * stat.blockSizeLong
        val freeGb = bytesAvailable.toDouble() / (1024 * 1024 * 1024)
        val totalGb = bytesTotal.toDouble() / (1024 * 1024 * 1024)

        val audio = audioService.inspectAudio()
        val network = networkService.inspectNetwork()

        val healthyCount = listOf(
            batteryPercent > 20,
            freeGb > 2.0,
            audio.issuesFound.isEmpty(),
            network.isConnected
        ).count { it }

        val overallScore = when (healthyCount) {
            4 -> "Excellent"
            3 -> "Good"
            2 -> "Needs Attention"
            else -> "Issues Detected"
        }

        return DeviceHealthReport(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            freeStorageGb = (freeGb * 10).toInt() / 10.0,
            totalStorageGb = (totalGb * 10).toInt() / 10.0,
            soundHealthy = audio.issuesFound.isEmpty(),
            networkHealthy = network.isConnected,
            overallScore = overallScore
        )
    }
}
