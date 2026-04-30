package com.example.tou
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val done: Boolean = false
)