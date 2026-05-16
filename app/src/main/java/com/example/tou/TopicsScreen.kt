package com.example.tou

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
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
                        Text(text = "›", color = Color.Gray)
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