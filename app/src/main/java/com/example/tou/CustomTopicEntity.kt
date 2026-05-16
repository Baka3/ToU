package com.example.tou

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CustomTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)