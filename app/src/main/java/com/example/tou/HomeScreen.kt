package com.example.tou
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tou.ClockWidget
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.tou.R
    @Composable
    fun HomeScreen(navController: NavController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.home_s2),
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(100.dp))

            ClockWidget()
            // віджет з годинником
        }
        /*Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(85.dp),
                contentAlignment = Alignment.Center
            ) {
                ClockWidget()
            }
            //Text("Hello")
        }*/
    }
