package com.example.tou

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ClockWidget() {

    var currentTime by remember {
        mutableStateOf(LocalDateTime.now())
    }

    LaunchedEffect(Unit) {

        while (true) {

            currentTime = LocalDateTime.now()

            delay(60000)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val dateFormatter = DateTimeFormatter.ofPattern(
        "EEEE, d MMMM",
        Locale.getDefault()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = currentTime.format(timeFormatter),
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = currentTime.format(dateFormatter),
            fontSize = 18.sp,
            color = Color.Gray
        )
    }
}