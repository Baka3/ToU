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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    val backgroundImage = remember {
        mutableIntStateOf(getBackgroundImage())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundImage.intValue),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()

            currentTime = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(now.time)

            currentDate = SimpleDateFormat(
                "EEEE, d MMMM",
                Locale("uk")
            ).format(now.time)

            backgroundImage.intValue = getBackgroundImage()

            delay(1000)
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
                Text(stringResource(R.string.nav_notes))
            }

            Button(
                onClick = {  navController.navigate("calendar") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.nav_calendar))
            }

            Button(
                onClick = { navController.navigate("settings") },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.nav_settings))
            }
        }
    }
}
fun getBackgroundImage(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 6..11 -> R.drawable.daytime
        in 12..17 -> R.drawable.fox2
        in 18..22 -> R.drawable.afternoon1
        else -> R.drawable.night
    }
}
