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
    val reminderDateTo: String = ""
)