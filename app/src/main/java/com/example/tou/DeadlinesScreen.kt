package com.example.tou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DeadlinesScreen(navController: NavController) {
    val notes by App.db.noteDao().getWithDate()
        .collectAsState(initial = emptyList())

    // беремо тільки нотатки з датою і групуємо по даті
    val grouped = notes
        .filter { it.date.isNotEmpty() }
        .groupBy { it.date }
        .toSortedMap(compareBy { parseDate(it) })

    if (grouped.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Немає нотаточок з термінами", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            grouped.forEach { (date, notesForDate) ->
                item {
                    // Лінія з датою посередині
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
                            text = date,
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
                                modifier = Modifier.padding(end = 8.dp),
                                color = if (note.done) Color.Gray else Color.Unspecified
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.text,
                                color = if (note.done) Color.Gray else Color.Unspecified,
                                style = if (note.done) {
                                    MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                }
                            )
                        }
                        if (note.time.isNotEmpty()) {
                            Text(
                                text = note.time,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                        /*if (note.time.isNotEmpty()) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = note.time, color = Color.Gray)
                        }*/
                    }
                }
            }
        }



// Парсимо дату "dd.MM.yyyy" для сортування
fun parseDate(dateStr: String): Long {
    return try {
        val parts = dateStr.split(".")
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()
        (year * 10000 + month * 100 + day).toLong()
    } catch (e: Exception) {
        0L
    }
}