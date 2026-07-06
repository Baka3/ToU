package com.example.tou

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
            private val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
                enableEdgeToEdge()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
                setContent {
                    val context = LocalContext.current
                    val selectedTheme by AppSettings.getTheme(context).collectAsState(initial = "system")

                    val darkTheme = when (selectedTheme) {
                        "dark" -> true
                        "light" -> false
                        else -> isSystemInDarkTheme()
                    }

                    val colorScheme = when (selectedTheme) {
                        "purple" -> if (darkTheme) darkColorScheme(
                            primary = Color(0xFFD0BCFF),
                            secondary = Color(0xFFCCC2DC)
                        ) else lightColorScheme(
                            primary = Color(0xFF6650A4),
                            secondary = Color(0xFF625B71)
                        )
                        "green" -> if (darkTheme) darkColorScheme(
                            primary = Color(0xFF9BD472),
                            secondary = Color(0xFFB5CCB0)
                        ) else lightColorScheme(
                            primary = Color(0xFF386A1F),
                            secondary = Color(0xFF52634D)
                        )
                        "blue" -> if (darkTheme) darkColorScheme(
                            primary = Color(0xFF9ECAFF),
                            secondary = Color(0xFFBBC7DB)
                        ) else lightColorScheme(
                            primary = Color(0xFF0061A4),
                            secondary = Color(0xFF535F70)
                        )
                        else -> if (darkTheme) darkColorScheme() else lightColorScheme()
                    }

                    MaterialTheme(colorScheme = colorScheme) {
                        val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("notes_menu") {
                            NotesMenuScreen(navController)
                        }
                        composable("notes_list") {
                            NotesScreen(navController)
                        }
                        composable("add_note_full") {
                            AddNoteFullScreen(navController = navController, defaultTopic = "")
                        }
                        composable("add_note_full/{defaultTopic}") { backStackEntry ->
                            val defaultTopic = backStackEntry.arguments?.getString("defaultTopic") ?: ""
                            AddNoteFullScreen(navController = navController, defaultTopic = defaultTopic)
                        }
                        composable(
                            route = "add_subtask/{parentNoteId}",
                        ) { backStackEntry ->
                            val parentNoteId = backStackEntry.arguments?.getString("parentNoteId")?.toInt()
                            AddNoteFullScreen(
                                navController = navController,
                                defaultTopic = "",
                                parentNoteId = parentNoteId
                            )
                        }
                        composable("edit/{noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")
                                ?.takeIf { it != "null" }
                                ?.toIntOrNull() ?: return@composable
                            EditNoteScreen(navController, noteId)
                        }
                        composable("deadlines") {
                            DeadlinesScreen(navController)
                        }
                        composable("completed") {
                            CompletedScreen(navController)
                        }
                        composable("topics") {
                            TopicsScreen(navController)
                        }
                        composable("topic_notes/{topic}") { backStackEntry ->
                            val topic = backStackEntry.arguments?.getString("topic") ?: return@composable
                            TopicNotesScreen(navController, topic)
                        }
                        composable("edit_subtask/{subtaskId}") { backStackEntry ->
                            val subtaskId = backStackEntry.arguments?.getString("subtaskId")?.toInt() ?: return@composable
                            EditSubtaskScreen(navController, subtaskId)
                        }
                        composable("reminders") {
                            RemindersScreen(navController)
                        }
                        composable("calendar") {
                            CalendarScreen(navController)
                        }
                        composable("settings") { SettingsScreen(navController) }
                        composable("settings_language") { LanguageSettingsScreen(navController) }
                        composable("settings_wallpaper") { WallpaperSettingsScreen(navController) }
                        composable("settings_notifications") { NotificationsSettingsScreen(navController) }
                        composable("settings_theme") { ThemeSettingsScreen(navController) }
                    }
                }
            }
        }
    }
