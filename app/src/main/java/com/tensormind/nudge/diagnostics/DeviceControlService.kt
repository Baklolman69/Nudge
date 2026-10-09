package com.tensormind.nudge.diagnostics

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings

class DeviceControlService(private val context: Context) {

    private var isTorchOn = false

    fun toggleTorch(): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return "No camera service found on this device."
            val cameraId = findFlashCameraId(cameraManager)
                ?: return "No flashlight hardware found on this device."
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            if (isTorchOn) "Flashlight turned ON" else "Flashlight turned OFF"
        } catch (e: Exception) {
            "Could not toggle flashlight: ${e.localizedMessage ?: "Camera unavailable"}"
        }
    }

    private fun findFlashCameraId(cameraManager: CameraManager): String? {
        // First look for back-facing camera with flash
        for (id in cameraManager.cameraIdList) {
            try {
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    return id
                }
            } catch (_: Exception) {}
        }
        // Fallback to any camera with flash
        for (id in cameraManager.cameraIdList) {
            try {
                val chars = cameraManager.getCameraCharacteristics(id)
                if (chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                    return id
                }
            } catch (_: Exception) {}
        }
        return null
    }

    fun isTorchActive(): Boolean = isTorchOn

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openAlarmSettings() {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_DATE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun openDialer(phoneNumber: String = "") {
        try {
            val uri = if (phoneNumber.isNotBlank()) Uri.parse("tel:$phoneNumber") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openApplicationSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
