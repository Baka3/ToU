package com.example.tou

//import com.example.tou.Note

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

@Composable
fun NotesScreen(navController: NavController) {
    val notesFromDb by App.db.noteDao().getActive()
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf(notesFromDb) }
    LaunchedEffect(notesFromDb) { notes = notesFromDb }

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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f)
        ) {
            items(notes, key = { it.id }) { note ->
                ReorderableItem(reorderableState, key = note.id) {
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
                        onEdit = { navController.navigate("edit/${note.id}") },
                        navController = navController,
                        dragHandle = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Перетягнути",
                                    modifier = Modifier.draggableHandle()
                                )
                            }
                        }
                        /*dragHandle = {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Перетягнути",
                                modifier = Modifier
                                    .draggableHandle()
                                    .padding(end = 4.dp)
                            )
                        }*/
                    )
                }
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
@Composable
fun NoteItem(
    note: NoteEntity,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    navController: NavController,
    dragHandle: @Composable (() -> Unit)? = null
) {
    val subtasks by App.db.subtaskDao().getByNote(note.id)
        .collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val noteOverdue = isOverdue(note.date, note.time) && !note.done

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.clickable { expanded = !expanded }
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
            dragHandle?.invoke()

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

            if (subtasks.isNotEmpty()) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            /*
            // ← стрілочка завжди видима
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            */
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
