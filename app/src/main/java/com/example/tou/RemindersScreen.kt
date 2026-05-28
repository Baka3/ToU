package com.example.tou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RemindersScreen(navController: NavController) {
    val notes by App.db.noteDao().getAll()
        .collectAsState(initial = emptyList())

    // беремо тільки нотатки з активними нагадуваннями
    val now = System.currentTimeMillis()

    val notesWithReminders = notes.filter { note ->
        when (note.reminderType) {
            "single" -> note.reminderDate.isNotEmpty() && note.reminderTime.isNotEmpty() &&
                    !isOverdue(note.reminderDate, note.reminderTime)
            "range" -> note.reminderDateTo.isNotEmpty() &&
                    !isOverdue(note.reminderDateTo, note.reminderTime)
            else -> false
        }
    }

    // групуємо по даті нагадування
    val grouped = notesWithReminders
        .groupBy { note ->
            when (note.reminderType) {
                "single" -> note.reminderDate
                "range" -> "${note.reminderDateFrom} — ${note.reminderDateTo}"
                else -> ""
            }
        }
        .toSortedMap(compareBy { key ->
            val date = key.split(" — ").first()
            parseDate(date)
        })

    if (grouped.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Активних нагадувань немає", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            grouped.forEach { (dateLabel, notesForDate) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                        Text(
                            text = dateLabel,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                    }
                }

                items(notesForDate) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.emoji.isNotEmpty()) {
                            Text(
                                text = note.emoji,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            when (note.reminderType) {
                                "single" -> Text(
                                    text = "${note.reminderDate} ${note.reminderTime}".trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                "range" -> Text(
                                    text = "Щодня о ${note.reminderTime} до ${note.reminderDateTo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}