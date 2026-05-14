package com.example.tou

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun EditNoteScreen(navController: NavController, noteId: Int) {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }

    LaunchedEffect(noteId) {
        val note = App.db.noteDao().getById(noteId)
        text = note?.text ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Редагувати нотатку...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (text.isNotBlank()) {
                scope.launch {
                    App.db.noteDao().update(NoteEntity(id = noteId, text = text))
                    navController.popBackStack()
                }
            }
        }) {
            Text("Зберегти")
        }
    }
}