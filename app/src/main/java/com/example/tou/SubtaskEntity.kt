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
    val done: Boolean = false
)