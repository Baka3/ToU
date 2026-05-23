package com.example.tou

import java.util.Calendar

fun isOverdue(date: String, time: String): Boolean {
    if (date.isEmpty()) return false
    return try {
        val parts = date.split(".")
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val year = parts[2].toInt()

        val deadline = Calendar.getInstance().apply {
            set(year, month, day)
            if (time.isNotEmpty()) {
                val timeParts = time.split(":")
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
        }
        Calendar.getInstance().after(deadline)
    } catch (e: Exception) {
        false
    }
}