package com.example.tou

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopicNotesScreen(navController: NavController, topic: String) {
    val notes by App.db.noteDao().getByTopic(topic)
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showMenu by remember { mutableStateOf(false) }
    var showTopicDialog by remember { mutableStateOf(false) }
    var showNewTopicDialog by remember { mutableStateOf(false) }
    var topicSearch by remember { mutableStateOf("") }
    var newTopicForSelected by remember { mutableStateOf("") }
    val allTopics by App.db.topicDao().getAll().collectAsState(initial = emptyList())
    val filteredTopics = remember(topicSearch, allTopics) {
        if (topicSearch.isEmpty()) allTopics
        else allTopics.filter { it.startsWith(topicSearch, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Верхня мінюшка в режимі вибору
        if (selectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    selectionMode = false
                    selectedIds = emptySet()
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Вийти")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${selectedIds.size}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Більше")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Виконати") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.Green
                                )
                            },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    selectedIds.forEach { id ->
                                        val note = notes.find { it.id == id }
                                        if (note != null) {
                                            App.db.noteDao().update(
                                                note.copy(
                                                    done = true,
                                                    completedAt = System.currentTimeMillis()
                                                )
                                            )
                                        }
                                    }
                                    selectedIds = emptySet()
                                    selectionMode = false
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Створити топік") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.EditNote, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                showNewTopicDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Додати до топіка") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                showTopicDialog = true
                            }
                        )
                    }
                }
                IconButton(onClick = {
                    scope.launch {
                        selectedIds.forEach { id ->
                            val note = notes.find { it.id == id }
                            if (note != null) App.db.noteDao().delete(note)
                        }
                        selectedIds = emptySet()
                        selectionMode = false
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Видалити",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            ScreenHeader(title = topic, navController = navController)
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notes, key = { it.id }) { note ->
                val isSelected = note.id in selectedIds
                NoteItem(
                    note = note,
                    onToggleDone = {
                        if (!selectionMode) {
                            scope.launch {
                                App.db.noteDao().update(
                                    note.copy(done = true, completedAt = System.currentTimeMillis())
                                )
                            }
                        }
                    },
                    onDelete = {
                        scope.launch { App.db.noteDao().delete(note) }
                    },
                    onEdit = {
                        if (!selectionMode) navController.navigate("edit/${note.id}")
                    },
                    navController = navController,
                    selectionMode = selectionMode,
                    isSelected = isSelected,
                    onLongPress = {
                        selectionMode = true
                        selectedIds = selectedIds + note.id
                    },
                    onSelect = {
                        selectedIds = if (note.id in selectedIds)
                            selectedIds - note.id
                        else
                            selectedIds + note.id
                    }
                )
            }
        }

        if (!selectionMode) {
            Button(
                onClick = { navController.navigate("add_note_full/$topic") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Додати нотаточку")
            }
        }
    }

    // Діалог вибору топіку
    if (showTopicDialog) {
        AlertDialog(
            onDismissRequest = { showTopicDialog = false },
            title = { Text("Додати до топіка") },
            text = {
                Column {
                    TextField(
                        value = topicSearch,
                        onValueChange = { topicSearch = it },
                        placeholder = { Text("Пошук топіку") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(filteredTopics) { t ->
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        selectedIds.forEach { id ->
                                            val note = notes.find { it.id == id }
                                            if (note != null) {
                                                App.db.noteDao().update(note.copy(topic = t))
                                                ensureTopicExists(t)
                                            }
                                        }
                                        selectedIds = emptySet()
                                        selectionMode = false
                                        showTopicDialog = false
                                        topicSearch = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(t)
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = {
                                    showTopicDialog = false
                                    showNewTopicDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Створити новий топік і зберегти туди")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopicDialog = false; topicSearch = "" }) {
                    Text("Скасувати")
                }
            },
            dismissButton = {}
        )
    }

    // Діалог створення нового топіку
    if (showNewTopicDialog) {
        AlertDialog(
            onDismissRequest = { showNewTopicDialog = false },
            title = { Text("Назва топіку") },
            text = {
                TextField(
                    value = newTopicForSelected,
                    onValueChange = { newTopicForSelected = it },
                    placeholder = { Text("Введіть назву") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTopicForSelected.isNotBlank()) {
                        scope.launch {
                            ensureTopicExists(newTopicForSelected.trim())
                            selectedIds.forEach { id ->
                                val note = notes.find { it.id == id }
                                if (note != null) {
                                    App.db.noteDao().update(
                                        note.copy(topic = newTopicForSelected.trim())
                                    )
                                }
                            }
                            selectedIds = emptySet()
                            selectionMode = false
                            showNewTopicDialog = false
                            newTopicForSelected = ""
                        }
                    }
                }) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTopicDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}