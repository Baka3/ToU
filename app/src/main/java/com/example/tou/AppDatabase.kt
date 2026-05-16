package com.example.tou
import androidx.room.*
@Database(entities = [NoteEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}