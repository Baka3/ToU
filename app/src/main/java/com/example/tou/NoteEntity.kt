package com.example.tou
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val done: Boolean = false,
    val emoji: String = "",
    val date: String = "",
    val time: String = "",
    val completedAt: Long = 0L,
    val topic: String = "",
    val description: String = "",
    val order: Int = 0,
    val reminderType: String = "", // "single" або "range"
    val reminderDate: String = "",
    val reminderTime: String = "",
    val reminderDateFrom: String = "",
    val reminderDateTo: String = "",
    val attachments: String = "",
    val reminderDates: String = "",        // JSON список дат
    val reminderTimes: String = "",        // JSON список часів
    val reminderRepeatType: String = "",   // "daily", "weekly", "yearly", "custom"
    val reminderRepeatCount: Int = 0,      // повторити N разів
    val reminderRepeatEveryHours: Int = 0, // кожні N годин
    val reminderUntilDate: String = "",    // до якої дати
    val reminderEndOfDay: Boolean = false  // нагадати до кінця дня

)