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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ToUTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "notes"
                ) {
                    composable("notes") {
                        NotesScreen(navController)
                    }
                    composable("add") {
                        AddNoteScreen(navController)
                    }
                    composable("edit/{noteId}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")?.toInt() ?: return@composable
                        EditNoteScreen(navController, noteId)
                    }
                    /*@Composable
                    fun NotesScreen(navController: NavController) {
                        NotesScreen(navController)
                    }

                    @Composable
                    fun AddNoteScreen(navController: NavController){
                        AddNoteScreen(navController)
                    }
                */
                }
            }
        }

    }
}
