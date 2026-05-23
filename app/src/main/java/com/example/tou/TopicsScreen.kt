package com.example.tou


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun TopicsScreen(navController: NavController) {
    val customTopics by App.db.topicDao().getAll()
        .collectAsState(initial = emptyList())
    val topicsFromNotes by App.db.noteDao().getTopics()
        .collectAsState(initial = emptyList())

    var reorderableTopics by remember { mutableStateOf(customTopics) }
    LaunchedEffect(customTopics) { reorderableTopics = customTopics }

    val allTopics = (reorderableTopics + topicsFromNotes).distinct()

    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renamingTopic by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        reorderableTopics = reorderableTopics.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        scope.launch {
            val maxOrder = App.db.topicDao().getMaxOrder() ?: 0
            App.db.topicDao().insert(
                CustomTopicEntity(name = newTopicName.trim(), order = maxOrder + 1)
            )
            newTopicName = ""
            showAddDialog = false
        }
}

Column(
modifier = Modifier
.fillMaxSize()
.padding(16.dp)
) {
    if (allTopics.isEmpty()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Топіків ще немає", color = Color.Gray)
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f)
        ) {
            items(allTopics, key = { it }) { topic ->
                val isCustom = topic in reorderableTopics
                ReorderableItem(
                    reorderableState,
                    key = topic,
                    enabled = isCustom
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("topic_notes/$topic") }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCustom) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Перетягнути",
                                modifier = Modifier
                                    .draggableHandle()
                                    .padding(end = 8.dp),
                                tint = Color.Gray
                            )
                        } else {
                            Spacer(modifier = Modifier.width(32.dp))
                        }

                        Text(
                            text = topic,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        if (isCustom) {
                            IconButton(onClick = {
                                renamingTopic = topic
                                newName = topic
                                showRenameDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Редагувати назву"
                                )
                            }
                            IconButton(onClick = {
                                scope.launch { App.db.topicDao().deleteByName(topic) }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Видалити",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Додати топік")
    }
}

// Діалог додавання топіку
if (showAddDialog) {
    AlertDialog(
        onDismissRequest = { showAddDialog = false },
        title = { Text("Новий топік") },
        text = {
            TextField(
                value = newTopicName,
                onValueChange = { newTopicName = it },
                placeholder = { Text("Назва топіку") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (newTopicName.isNotBlank()) {
                    scope.launch {
                        App.db.topicDao().insert(
                            CustomTopicEntity(name = newTopicName.trim())
                        )
                        newTopicName = ""
                        showAddDialog = false
                    }
                }
            }) {
                Text("Додати")
            }
        },
        dismissButton = {
            TextButton(onClick = { showAddDialog = false }) {
                Text("Скасувати")
            }
        }
    )
}

// Діалог перейменування топіку
if (showRenameDialog) {
    AlertDialog(
        onDismissRequest = { showRenameDialog = false },
        title = { Text("Змінити назву") },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (newName.isNotBlank() && newName != renamingTopic) {
                    scope.launch {
                        App.db.topicDao().rename(renamingTopic, newName.trim())
                        App.db.noteDao().renameTopic(renamingTopic, newName.trim())
                        showRenameDialog = false
                    }
                } else {
                    showRenameDialog = false
                }
            }) {
                Text("Зберегти")
            }
        },
        dismissButton = {
            TextButton(onClick = { showRenameDialog = false }) {
                Text("Скасувати")
            }
        }
    )
}
}
 /*@Composable
fun TopicsScreen(navController: NavController) {
    val topics by App.db.noteDao().getTopics()
        .collectAsState(initial = emptyList())
    val customTopics by App.db.topicDao().getAll() // ← виправлено App.db.App.db
        .collectAsState(initial = emptyList())

    val allTopics = (customTopics + topics).distinct()

    var showDialog by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope() // ← перенесли вгору

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (allTopics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Топіків ще немає", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allTopics) { topic ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("topic_notes/$topic") }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            scope.launch {
                                App.db.topicDao().deleteByName(topic)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Видалити топік",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Додати топік")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Новий топік") },
                text = {
                    TextField(
                        value = newTopicName,
                        onValueChange = { newTopicName = it },
                        placeholder = { Text("Назва топіку") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newTopicName.isNotBlank()) {
                            scope.launch {
                                App.db.topicDao().insert( // ← виправлено
                                    CustomTopicEntity(name = newTopicName.trim())
                                )
                                newTopicName = ""
                                showDialog = false
                            }
                        }
                    }) {
                        Text("Додати")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Скасувати")
                    }
                }
            )
        }
    }
}
*/