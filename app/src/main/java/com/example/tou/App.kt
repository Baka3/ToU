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
    }

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }

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
}