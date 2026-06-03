package com.example.tou
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_s2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Картинка розтягнеться на заповнить екран
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            currentDate = SimpleDateFormat("EEEE, d MMMM", Locale("uk")).format(now.time)
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Годинник з тінню для читабельності
            Box(
                modifier = Modifier
                    .padding(bottom = 225.dp),
                contentAlignment = Alignment.Center
            ) {
                // розмитий фон під текстом
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTime,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = currentDate,
                        fontSize = 16.sp,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
            }

            // Кнопки
            Button(
                onClick = { navController.navigate("notes_menu") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text("Нотаточки")
            }

            Button(
                onClick = {  navController.navigate("calendar") },
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
}
