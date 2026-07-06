package com.example.tou

//import com.example.tou.Note

import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.tou.SubtaskEntity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton

@Composable
fun NotesScreen(navController: NavController) {
    val notesFromDb by App.db.noteDao().getActive()
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf(notesFromDb) }
    LaunchedEffect(notesFromDb) { notes = notesFromDb }

    // режим вибору
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        notes = notes.toMutableList().apply { add(to.index, removeAt(from.index)) }
        scope.launch {
            notes.forEachIndexed { index, note ->
                App.db.noteDao().updateOrder(note.id, index)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Верхня мінюшка в режимі вибору
        if (selectionMode) {
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

                Text(
                    text = "${selectedIds.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Три крапки
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

                // Смітник
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
                                items(filteredTopics) { topic ->
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                selectedIds.forEach { id ->
                                                    val note = notes.find { it.id == id }
                                                    if (note != null) {
                                                        App.db.noteDao().update(note.copy(topic = topic))
                                                        ensureTopicExists(topic)
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
                                        Text(topic)
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

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f)
        ) {
            items(notes, key = { it.id }) { note ->
                ReorderableItem(reorderableState, key = note.id) {
                    val isSelected = note.id in selectedIds

                    NoteItem(
                        note = note,
                        onToggleDone = {
                            if (!selectionMode) {
                                scope.launch {
                                    App.db.noteDao().update(
                                        note.copy(
                                            done = true,
                                            completedAt = System.currentTimeMillis()
                                        )
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
                            selectedIds = if (note.id in selectedIds) {
                                selectedIds - note.id
                            } else {
                                selectedIds + note.id
                            }
                        },
                        dragHandle = if (selectionMode) {
                            {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Перетягнути",
                                    modifier = Modifier
                                        .draggableHandle()
                                        .padding(end = 4.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        if (!selectionMode) {
            Button(
                onClick = { navController.navigate("add_note_full") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Додати нотатку")
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

        val navController = null
        //ScreenHeader(title = "Редагування нотатки", navController = navController)

        Text(
            text = subtask.title,
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() },
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    navController: NavController,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onSelect: () -> Unit = {},
    dragHandle: @Composable (() -> Unit)? = null
) {
    val subtasks by App.db.subtaskDao().getByNote(note.id)
        .collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val noteOverdue = isOverdue(note.date, note.time) && !note.done

    val backgroundColor = when {
        isSelected -> Color.Green.copy(alpha = 0.15f)
        noteOverdue -> Color.Red.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = {
                    if (selectionMode) onSelect()
                    else expanded = !expanded
                },
                onLongClick = {
                    if (!selectionMode) onLongPress()
                }
            )
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dragHandle?.invoke()

            if (!selectionMode) {
                Checkbox(
                    checked = note.done,
                    onCheckedChange = { onToggleDone() }
                )
            } else {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
            }

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

            if (!selectionMode) {
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
        }

        if (!selectionMode) {
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
                            onDelete = {
                                scope.launch { App.db.subtaskDao().delete(subtask) }
                            }
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate("add_subtask/${note.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Додати підтаску")
                    }
                }
            }
        }
    }
}
