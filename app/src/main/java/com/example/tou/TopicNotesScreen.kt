package com.example.tou

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
import kotlinx.coroutines.launch

@Composable
fun TopicNotesScreen(navController: NavController, topic: String) {
    val notes by App.db.noteDao().getByTopic(topic)
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = topic,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
                    onToggleDone = {
                        scope.launch {
                            App.db.noteDao().update(
                                note.copy(done = true, completedAt = System.currentTimeMillis())
                            )
                        }
                    },
                    onDelete = {
                        scope.launch { App.db.noteDao().delete(note) }
                    },
                    onEdit = {
                        navController.navigate("edit/${note.id}")
                    }
                )
            }
        }
    }
}