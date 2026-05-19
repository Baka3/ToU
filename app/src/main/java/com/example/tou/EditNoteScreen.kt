package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.heightIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(navController: NavController, noteId: Int) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var noteText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("") }
    var topicText by remember { mutableStateOf("") }
    var showEmojiField by remember { mutableStateOf(false) }
    var showTopicDropdown by remember { mutableStateOf(false) }
    var subtasks by remember { mutableStateOf(listOf<SubtaskDraft>()) }
    var description by remember { mutableStateOf("") }
    val existingTopics by App.db.noteDao().getTopics()
        .collectAsState(initial = emptyList())

    LaunchedEffect(noteId) {
        val note = App.db.noteDao().getById(noteId)
        noteText = note?.text ?: ""
        selectedDate = note?.date ?: ""
        selectedTime = note?.time ?: ""
        selectedEmoji = note?.emoji ?: ""
        topicText = note?.topic ?: ""
        description = note?.description ?: ""
        val existingSubtasks = App.db.subtaskDao().getByNoteOnce(noteId)
        subtasks = existingSubtasks.map {
            SubtaskDraft(
                id = it.id,
                title = it.title,
                description = it.description,
                date = it.date,
                time = it.time,
                expanded = false
            )
        }
    }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> selectedDate = "$day.${month + 1}.$year" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> selectedTime = "%02d:%02d".format(hour, minute) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Нотаточка
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Нотаточка", modifier = Modifier.width(100.dp))
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Термін
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Термін", modifier = Modifier.width(100.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(56.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { datePickerDialog.show() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (selectedDate.isEmpty()) "Оберіть дату" else selectedDate,
                        modifier = Modifier.padding(start = 16.dp),
                        color = if (selectedDate.isEmpty()) Color.Gray else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(56.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { timePickerDialog.show() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (selectedTime.isEmpty()) "Оберіть час" else selectedTime,
                        modifier = Modifier.padding(start = 16.dp),
                        color = if (selectedTime.isEmpty()) Color.Gray else Color.Unspecified
                    )
                }
            }
        }

        // Топік
        val allTopicsForDropdown by App.db.topicDao().getAll()
            .collectAsState(initial = emptyList())
        val topicsFromNotes by App.db.noteDao().getTopics()
            .collectAsState(initial = emptyList())
        val allAvailableTopics = (allTopicsForDropdown + topicsFromNotes).distinct()

        val filteredTopics = remember(topicText, allAvailableTopics) {
            if (topicText.isEmpty()) allAvailableTopics
            else allAvailableTopics.filter {
                it.startsWith(topicText, ignoreCase = true)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Топік", modifier = Modifier.width(100.dp))
            Column(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = showTopicDropdown && filteredTopics.isNotEmpty(),
                    onExpandedChange = { showTopicDropdown = it }
                ) {
                    TextField(
                        value = topicText,
                        onValueChange = {
                            topicText = it
                            showTopicDropdown = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        singleLine = true,
                        placeholder = { Text("Введіть або оберіть топік") },
                        trailingIcon = {
                            if (allAvailableTopics.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = showTopicDropdown
                                )
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showTopicDropdown && filteredTopics.isNotEmpty(),
                        onDismissRequest = { showTopicDropdown = false },
                        modifier = Modifier.heightIn(max = 200.dp) // ← обмежуємо висоту, всередині буде скрол
                    ) {
                        filteredTopics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic) },
                                onClick = {
                                    topicText = topic
                                    showTopicDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Іконка
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Іконка", modifier = Modifier.width(100.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (selectedEmoji.isNotEmpty()) {
                    Text(text = selectedEmoji, fontSize = 32.sp, modifier = Modifier.padding(end = 8.dp))
                }
                OutlinedButton(onClick = { showEmojiField = !showEmojiField }) {
                    Text(if (selectedEmoji.isEmpty()) "Обрати емодзі" else "Змінити")
                }
            }
        }

        if (showEmojiField) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = selectedEmoji,
                    onValueChange = { if (it.length <= 2) selectedEmoji = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введіть емодзі") },
                    singleLine = true
                )
                TextButton(onClick = { showEmojiField = false }) { Text("Ок") }
            }
        }

        //Опис
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Опис", modifier = Modifier.width(100.dp).padding(top = 16.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.weight(1f),
                minLines = 3
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SubtasksSection(
            subtasks = subtasks,
            onSubtasksChange = { subtasks = it }
        )
        /*Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Додати підтаски")
        }*/

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (noteText.isNotBlank()) {
                    scope.launch {
                        App.db.noteDao().update(
                            NoteEntity(
                                id = noteId,
                                text = noteText,
                                emoji = selectedEmoji,
                                date = selectedDate,
                                time = selectedTime,
                                topic = topicText,
                                description = description
                            )
                        )
                        // видаляємо старі підтаски і вставляємо оновлені
                        App.db.subtaskDao().deleteByNote(noteId)
                        subtasks.forEach { subtask ->
                            App.db.subtaskDao().insert(
                                SubtaskEntity(
                                    parentNoteId = noteId,
                                    title = subtask.title,
                                    description = subtask.description,
                                    date = subtask.date,
                                    time = subtask.time
                                )
                            )
                        }
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зберегти")
        }
    }
}