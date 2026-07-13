package com.example.tou

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Нагадування"
        val noteId = intent.getIntExtra("noteId", 0)
        val deadline = intent.getStringExtra("deadline") ?: ""
        val description = intent.getStringExtra("description") ?: ""

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "reminders",
            "Нагадування",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }
        manager.createNotificationChannel(channel)

        // Intent щоб відкрити додаток при натисненні
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("noteId", noteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Натисніть щоб відкрити")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)

        // Розгорнуте повідомлення
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        if (deadline.isNotEmpty()) {
            inboxStyle.addLine("📅 Дедлайн: $deadline")
        }
        if (description.isNotEmpty()) {
            inboxStyle.addLine("📝 $description")
        }

        builder.setStyle(inboxStyle)

        manager.notify(noteId, builder.build())
    }
}