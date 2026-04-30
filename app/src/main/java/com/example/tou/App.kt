package com.example.tou

import android.app.Application
import androidx.room.Room
import com.example.tou.AppDatabase

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
        ).build()
    }
}