package com.example.tou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tou.ui.theme.ToUTheme
import androidx.navigation.NavController
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
                ToUTheme {

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
                    }
                }
            }
        }
    }
