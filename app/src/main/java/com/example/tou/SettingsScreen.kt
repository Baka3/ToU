package com.example.tou

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.nav_settings), navController = navController)

        SettingsItem(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language),
            subtitle = stringResource(R.string.subtitle_lang_ukrainian),
            onClick = { navController.navigate("settings_language") }
        )

        SettingsItem(
            icon = Icons.Default.Wallpaper,
            title = stringResource(R.string.settings_wallpaper),
            subtitle = stringResource(R.string.subtitle_change_wallpaper),
            onClick = { navController.navigate("settings_wallpaper") }
        )

        SettingsItem(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.reminders),
            subtitle = stringResource(R.string.subtitle_notifications),
            onClick = { navController.navigate("settings_notifications") }
        )

        SettingsItem(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_theme),
            subtitle = stringResource(R.string.subtitle_color_scheme),
            onClick = { navController.navigate("settings_theme") }
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Gray
        )
    }
    HorizontalDivider(color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f))
}