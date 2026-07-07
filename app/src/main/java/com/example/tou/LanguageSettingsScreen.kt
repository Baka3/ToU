package com.example.tou

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun LanguageSettingsScreen(navController: NavController) {
    val languages = listOf(
        "uk" to "Українська",
        "en" to "English",
        "de" to "Deutsch",
        "es" to "Español"
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedLang by AppSettings.getLanguage(context).collectAsState(initial = "uk")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Мова", navController = navController)

        languages.forEach { (code, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            AppSettings.setLanguage(context, code)
                            (context as? Activity)?.recreate()
                        }
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (selectedLang == code) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Повна локалізація буде додана в наступному оновленні",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}