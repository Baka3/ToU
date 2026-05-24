package com.example.tou

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

fun scheduleReminder(
    context: Context,
    noteId: Int,
    noteTitle: String,
    date: String,
    time: String
) {
    if (date.isEmpty() || time.isEmpty()) return

    try {
        val parts = date.split(".")
        val timeParts = time.split(":")
        val calendar = Calendar.getInstance().apply {
            set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", noteTitle)
            putExtra("noteId", noteId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun scheduleRangeReminders(
    context: Context,
    noteId: Int,
    noteTitle: String,
    dateFrom: String,
    dateTo: String,
    time: String
) {
    if (dateFrom.isEmpty() || dateTo.isEmpty() || time.isEmpty()) return

    try {
        val fromParts = dateFrom.split(".")
        val toParts = dateTo.split(".")
        val timeParts = time.split(":")

        val from = Calendar.getInstance().apply {
            set(fromParts[2].toInt(), fromParts[1].toInt() - 1, fromParts[0].toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val to = Calendar.getInstance().apply {
            set(toParts[2].toInt(), toParts[1].toInt() - 1, toParts[0].toInt())
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        var current = from.clone() as Calendar
        var requestCode = noteId * 1000 // унікальний код для кожного дня

        while (!current.after(to)) {
            val trigger = current.clone() as Calendar
            trigger.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            trigger.set(Calendar.MINUTE, timeParts[1].toInt())
            trigger.set(Calendar.SECOND, 0)

            if (trigger.timeInMillis > System.currentTimeMillis()) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("title", noteTitle)
                    putExtra("noteId", noteId)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.timeInMillis,
                    pendingIntent
                )
            }

            current.add(Calendar.DAY_OF_MONTH, 1)
            requestCode++
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun cancelReminder(context: Context, noteId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        noteId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}