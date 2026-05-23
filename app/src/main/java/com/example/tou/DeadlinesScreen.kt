package com.example.tou

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(
                            text = date,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }
                }

                items(notesForDate) { note ->
                    val subtasks by App.db.subtaskDao().getByNote(note.id)
                        .collectAsState(initial = emptyList())
                    val subtasksWithDate = subtasks.filter { it.date.isNotEmpty() }
                    var expanded by remember { mutableStateOf(false) }

                    val noteOverdue = isOverdue(note.date, note.time) && !note.done
                    val hasOverdueSubtask = subtasksWithDate.any {
                        isOverdue(it.date, it.time) && !it.done
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (noteOverdue) Modifier.background(Color.Red.copy(alpha = 0.1f))
                                else Modifier
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    color = when {
                                        note.done -> Color.Gray
                                        noteOverdue -> Color.Red
                                        else -> Color.Unspecified
                                    },
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
                                    color = if (noteOverdue) Color.Red else Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // знак оклику якщо є просрочена підтаска але сама таска не просрочена
                            if (hasOverdueSubtask && !noteOverdue && subtasksWithDate.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Є просрочені підтаски",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(start = 4.dp)
                                )
                            }

                            if (subtasksWithDate.isNotEmpty()) {
                                IconButton(onClick = { expanded = !expanded }) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column(modifier = Modifier.padding(start = 32.dp)) {
                                subtasksWithDate.forEach { subtask ->
                                    val subtaskOverdue = isOverdue(subtask.date, subtask.time) && !subtask.done
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (subtaskOverdue) Modifier.background(Color.Red.copy(alpha = 0.1f))
                                                else Modifier
                                            )
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = subtask.title,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = when {
                                                subtask.done -> Color.Gray
                                                subtaskOverdue -> Color.Red
                                                else -> Color.Unspecified
                                            }
                                        )
                                        Text(
                                            text = "${subtask.date} ${subtask.time}".trim(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (subtaskOverdue) Color.Red else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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