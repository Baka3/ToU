package com.example.tou

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object AppSettings {
    val THEME_KEY = stringPreferencesKey("theme")
    val LANGUAGE_KEY = stringPreferencesKey("language")

    fun getTheme(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME_KEY] ?: "system" }

    suspend fun setTheme(context: Context, theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    fun getLanguage(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE_KEY] ?: "uk" }

    suspend fun setLanguage(context: Context, lang: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
    }
}