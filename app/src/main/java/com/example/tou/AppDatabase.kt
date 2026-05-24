package com.example.tou
import androidx.room.*
@Database(entities = [NoteEntity::class, SubtaskEntity::class, CustomTopicEntity::class], version = 9)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun subtaskDao(): SubtaskDao

    abstract fun topicDao(): TopicDao
}