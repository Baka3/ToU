package com.example.tou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

//import com.example.tou.Note

@Composable
fun NotesScreen(navController: NavController) {
    val notes by App.db.noteDao().getAll()
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
                            App.db.noteDao().update(note.copy(done = !note.done))
                        }
                    },
                    onDelete = {
                        scope.launch {
                            App.db.noteDao().delete(note)
                        }
                    }
                )
            }
        }

        Button(
            onClick = { navController.navigate("add") },
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
            onDelete: () -> Unit
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = note.done,
                    onCheckedChange = { onToggleDone() }
                )

                Text(
                    text = note.text,
                    modifier = Modifier.weight(1f),
                    style = if (note.done) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }
                )

                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Видалити",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

    /*Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // СПИСОК
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(notes) { note ->
                Text(note.text)


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = note.done,
                            onCheckedChange = { checked ->
                                val index = notes.indexOf(note)
                                notes[index] = note.copy(done = checked)
                            }
                        )

                        Text(
                            text = note.text,
                            modifier = Modifier.weight(1f)
                        )

                        Button(onClick = {
                            notes.remove(note)
                        }) {
                            Text("X")
                        }
                    }
                }
            }
        }

        // ВВІД ТЕКСТУ
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth()
        )

        // КНОПКА ДОДАТИ
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    notes.add(Note(text.trim()))
                    text = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати")
        }
    }
}*/