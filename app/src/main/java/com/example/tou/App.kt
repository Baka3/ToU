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
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN reminderType TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN reminderDate TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN reminderTime TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN reminderDateFrom TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN reminderDateTo TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN emoji TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN topic TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN reminderType TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN reminderDate TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN reminderTime TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN reminderDateFrom TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN reminderDateTo TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE NoteEntity ADD COLUMN attachments TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE SubtaskEntity ADD COLUMN attachments TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Перестворюємо NoteEntity без imagePath
                database.execSQL("""
            CREATE TABLE NoteEntity_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                text TEXT NOT NULL,
                done INTEGER NOT NULL DEFAULT 0,
                emoji TEXT NOT NULL DEFAULT '',
                date TEXT NOT NULL DEFAULT '',
                time TEXT NOT NULL DEFAULT '',
                completedAt INTEGER NOT NULL DEFAULT 0,
                topic TEXT NOT NULL DEFAULT '',
                description TEXT NOT NULL DEFAULT '',
                `order` INTEGER NOT NULL DEFAULT 0,
                reminderType TEXT NOT NULL DEFAULT '',
                reminderDate TEXT NOT NULL DEFAULT '',
                reminderTime TEXT NOT NULL DEFAULT '',
                reminderDateFrom TEXT NOT NULL DEFAULT '',
                reminderDateTo TEXT NOT NULL DEFAULT '',
                attachments TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

                // Копіюємо дані, imagePath ігноруємо
                database.execSQL("""
            INSERT INTO NoteEntity_new (
                id, text, done, emoji, date, time, completedAt, topic, description,
                `order`, reminderType, reminderDate, reminderTime, reminderDateFrom,
                reminderDateTo, attachments
            )
            SELECT 
                id, text, done, emoji, date, time, completedAt, topic, description,
                `order`, reminderType, reminderDate, reminderTime, reminderDateFrom,
                reminderDateTo, attachments
            FROM NoteEntity
        """.trimIndent())

                database.execSQL("DROP TABLE NoteEntity")
                database.execSQL("ALTER TABLE NoteEntity_new RENAME TO NoteEntity")

                // Те саме для SubtaskEntity
                database.execSQL("""
            CREATE TABLE SubtaskEntity_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                parentNoteId INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                date TEXT NOT NULL DEFAULT '',
                time TEXT NOT NULL DEFAULT '',
                done INTEGER NOT NULL DEFAULT 0,
                emoji TEXT NOT NULL DEFAULT '',
                topic TEXT NOT NULL DEFAULT '',
                reminderType TEXT NOT NULL DEFAULT '',
                reminderDate TEXT NOT NULL DEFAULT '',
                reminderTime TEXT NOT NULL DEFAULT '',
                reminderDateFrom TEXT NOT NULL DEFAULT '',
                reminderDateTo TEXT NOT NULL DEFAULT '',
                attachments TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

                database.execSQL("""
            INSERT INTO SubtaskEntity_new (
                id, parentNoteId, title, description, date, time, done, emoji, topic,
                reminderType, reminderDate, reminderTime, reminderDateFrom, reminderDateTo,
                attachments
            )
            SELECT 
                id, parentNoteId, title, description, date, time, done, emoji, topic,
                reminderType, reminderDate, reminderTime, reminderDateFrom, reminderDateTo,
                attachments
            FROM SubtaskEntity
        """.trimIndent())

                database.execSQL("DROP TABLE SubtaskEntity")
                database.execSQL("ALTER TABLE SubtaskEntity_new RENAME TO SubtaskEntity")
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
            MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
            MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
            MIGRATION_11_12, MIGRATION_12_13
        ).build()
    }
    suspend fun ensureTopicExists(name: String) {
        if (name.isNotBlank() && App.db.topicDao().exists(name) == 0) {
            App.db.topicDao().insert(CustomTopicEntity(name = name))
        }
    }
}