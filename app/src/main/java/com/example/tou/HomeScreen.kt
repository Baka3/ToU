package com.example.tou
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

    @Composable
    fun HomeScreen(navController: NavController) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("notes_menu") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Нотаточки")
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Календар")
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Налаштування")
            }
        }
    }
