package com.example.tou

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedTheme by AppSettings.getTheme(context).collectAsState(initial = "system")

    val themes = listOf(
        Triple("system", stringResource(R.string.theme_system), Color.Gray),
        Triple("light", stringResource(R.string.theme_light), Color.White),
        Triple("dark", stringResource(R.string.theme_dark), Color.DarkGray),
        Triple("purple", stringResource(R.string.theme_purple), Color(0xFF6650A4)),
        Triple("green", stringResource(R.string.theme_green), Color(0xFF386A1F)),
        Triple("blue", stringResource(R.string.theme_blue), Color(0xFF0061A4))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        themes.forEach { (code, name, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { AppSettings.setTheme(context, code) }
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedTheme == code) 2.dp else 1.dp,
                            color = if (selectedTheme == code) MaterialTheme.colorScheme.primary
                            else Color.LightGray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedTheme == code) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (color == Color.White) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        }
    }
}