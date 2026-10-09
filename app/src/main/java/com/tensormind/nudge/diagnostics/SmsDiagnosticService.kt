package com.tensormind.nudge.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsMessageItem(
    val id: Long,
    val sender: String,
    val body: String,
    val dateString: String,
    val isSuspiciousHint: Boolean
)

class SmsDiagnosticService(private val context: Context) {

    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRecentMessages(limit: Int = 5): List<SmsMessageItem> {
        if (!hasSmsPermission()) {
            return getSampleMessages()
        }

        val messages = mutableListOf<SmsMessageItem>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow("_id")
                val addrCol = it.getColumnIndexOrThrow("address")
                val bodyCol = it.getColumnIndexOrThrow("body")
                val dateCol = it.getColumnIndexOrThrow("date")

                val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

                while (it.moveToNext() && messages.size < limit) {
                    val id = it.getLong(idCol)
                    val sender = it.getString(addrCol) ?: "Unknown"
                    val body = it.getString(bodyCol) ?: ""
                    val timestamp = it.getLong(dateCol)
                    val formattedDate = dateFormat.format(Date(timestamp))

                    val lower = body.lowercase()
                    val suspicious = lower.contains("bank") || lower.contains("urgent") ||
                            lower.contains("locked") || lower.contains("bit.ly") ||
                            lower.contains("suspended") || lower.contains("verify")

                    messages.add(
                        SmsMessageItem(
                            id = id,
                            sender = sender,
                            body = body,
                            dateString = formattedDate,
                            isSuspiciousHint = suspicious
                        )
                    )
                }
            }
        } catch (_: Exception) {
            return getSampleMessages()
        }

        return if (messages.isEmpty()) getSampleMessages() else messages
    }

    private fun getSampleMessages(): List<SmsMessageItem> {
        return listOf(
            SmsMessageItem(
                id = 1L,
                sender = "+1 (888) 392-1092",
                body = "URGENT CHASE ALERT: Your checking card has been locked due to unauthorized activity. Verify identity at bit.ly/chase-auth-92 to restore access.",
                dateString = "Today, 11:24 AM",
                isSuspiciousHint = true
            ),
            SmsMessageItem(
                id = 2L,
                sender = "USPS Tracking",
                body = "USPS: Package delivery failure. Redelivery fee of $1.99 required. Confirm address: usps-postage-fee.com/track",
                dateString = "Yesterday, 4:15 PM",
                isSuspiciousHint = true
            ),
            SmsMessageItem(
                id = 3L,
                sender = "Sarah (Granddaughter)",
                body = "Hi Grandpa! Just checking in, hope you are having a wonderful week! Let me know if you need anything from the grocery store.",
                dateString = "Yesterday, 2:00 PM",
                isSuspiciousHint = false
            ),
            SmsMessageItem(
                id = 4L,
                sender = "CVS Pharmacy",
                body = "Your prescription #49281 is ready for pickup at Main Street branch. Refills available.",
                dateString = "Oct 6, 9:30 AM",
                isSuspiciousHint = false
            )
        )
    }
}