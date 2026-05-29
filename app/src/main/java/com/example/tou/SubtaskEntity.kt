package com.example.tou

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentNoteId: Int,
    val title: String,
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val done: Boolean = false,
    val emoji: String = "",
    val topic: String = "",
    val attachments: String = "",
    val reminderType: String = "",
    val reminderDate: String = "",
    val reminderTime: String = "",
    val reminderDateFrom: String = "",
    val reminderDateTo: String = ""
)