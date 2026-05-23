package com.example.tou

import android.app.Application
import androidx.room.Room
import com.example.tou.AppDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class App : Application() {

    companion object {
        lateinit var db: AppDatabase
            private set

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN emoji TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN date TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN time TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN completedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN topic TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS SubtaskEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        parentNoteId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        date TEXT NOT NULL DEFAULT '',
                        time TEXT NOT NULL DEFAULT '',
                        done INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS CustomTopicEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CustomTopicEntity ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        ).addMigrations(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8
        ).build()
    }
    suspend fun ensureTopicExists(name: String) {
        if (name.isNotBlank() && App.db.topicDao().exists(name) == 0) {
            App.db.topicDao().insert(CustomTopicEntity(name = name))
        }
    }
}