package com.example.tou

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.tou.R
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun NotesMenuScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_scr),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Картинка розтягнеться на заповнить екран
        )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("notes_list") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Список всіх справ")
        }

        Button(
            onClick = { navController.navigate("deadlines") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Терміни")
        }

        Button(
            onClick = { navController.navigate("reminders") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Нагадування")
        }

        Button(
            onClick = { navController.navigate("topics") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Топіки")
        }

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Спільні нотаточки")
        }

        Button(
            onClick = { navController.navigate("completed") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp)
        ) {
            Text("Виконані справи")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Головний контейнер для кнопки
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            // Розмита підкладка (малюється тільки фон)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .blur(15.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            // Чітка кнопка (малюється поверх розмитого шару)
            OutlinedButton(
                onClick = { navController.navigate("add_note_full") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f), // Легкий тон поверх розмиття
                    contentColor = Color(0xFF222222) // Темний чіткий текст
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Додати нотаточку")
            }
            /*
        // Кнопка з пунктиром
        OutlinedButton(
            onClick = { navController.navigate("add_note_full") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Gray
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Додати нотаточку")
        }
         */
        }
    }
}