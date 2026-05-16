package com.example.tou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.Color
//import com.example.tou.Note
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.unit.sp


@Composable
fun NotesScreen(navController: NavController) {
    val notes by App.db.noteDao().getActive()
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
                    onToggleDone = {
                        scope.launch {
                            App.db.noteDao().update(
                                note.copy(
                                    done = true,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    },
                    onDelete = {
                        scope.launch { App.db.noteDao().delete(note) }
                    },
                    onEdit = {
                        navController.navigate("edit/${note.id}")
                    },
                    navController = navController
                )
            }
        }

        Button(
            onClick = { navController.navigate("add_note_full") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати нотатку")
        }
    }
}
@Composable
fun NoteItem(
    note: NoteEntity,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    navController: NavController
) {
    val subtasks by App.db.subtaskDao().getByNote(note.id)
        .collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = note.done,
                onCheckedChange = { onToggleDone() }
            )

            if (note.emoji.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = note.emoji, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = note.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            // стрілочка тільки якщо є підтаски
            if (subtasks.isNotEmpty()) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            IconButton(onClick = { onEdit() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Редагувати")
            }

            IconButton(onClick = { onDelete() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Видалити",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Розгорнуті підтаски
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp)
            ) {
                subtasks.forEach { subtask ->
                    SubtaskItem(
                        subtask = subtask,
                        onEdit = { navController.navigate("edit_subtask/${subtask.id}") },
                        onDelete = {  }
                    )
                }
            }
        }
    }
}

@Composable
fun SubtaskItem(
    subtask: SubtaskEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subtask.done,
            onCheckedChange = {
                scope.launch {
                    App.db.subtaskDao().update(subtask.copy(done = !subtask.done))
                }
            }
        )
        Text(
            text = subtask.title,
            modifier = Modifier.weight(1f),
            style = if (subtask.done) {
                MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.LineThrough,
                    color = Color.Gray
                )
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )
        if (subtask.date.isNotEmpty()) {
            Text(
                text = subtask.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        IconButton(onClick = { onEdit() }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Редагувати",
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = {
            scope.launch { App.db.subtaskDao().delete(subtask) }
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Видалити",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}