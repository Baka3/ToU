package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(navController: NavController, noteId: Int) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var noteText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("") }
    var topicText by remember { mutableStateOf("") }
    var showEmojiField by remember { mutableStateOf(false) }
    var showTopicDropdown by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var reminderType by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }
    var reminderDateFrom by remember { mutableStateOf("") }
    var reminderDateTo by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf(listOf<String>()) }

    val subtasksFromDb by App.db.subtaskDao().getByNote(noteId)
        .collectAsState(initial = emptyList())

    LaunchedEffect(noteId) {
        val note = App.db.noteDao().getById(noteId)
        noteText = note?.text ?: ""
        selectedDate = note?.date ?: ""
        selectedTime = note?.time ?: ""
        selectedEmoji = note?.emoji ?: ""
        topicText = note?.topic ?: ""
        description = note?.description ?: ""
        reminderType = note?.reminderType ?: ""
        reminderDate = note?.reminderDate ?: ""
        reminderTime = note?.reminderTime ?: ""
        reminderDateFrom = note?.reminderDateFrom ?: ""
        reminderDateTo = note?.reminderDateTo ?: ""
        attachments = decodeAttachments(note?.attachments ?: "")
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

    val allTopicsForDropdown by App.db.topicDao().getAll()
        .collectAsState(initial = emptyList())
    val filteredTopics = remember(topicText, allTopicsForDropdown) {
        if (topicText.isEmpty()) allTopicsForDropdown
        else allTopicsForDropdown.filter { it.startsWith(topicText, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        ScreenHeader(title = "Редагування нотатки", navController = navController)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f).height(56.dp)
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
                    if (selectedDate.isNotEmpty()) {
                        IconButton(onClick = { selectedDate = ""; selectedTime = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Очистити")
                        }
                    }
                }
                if (selectedDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f).height(56.dp)
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
                        if (selectedTime.isNotEmpty()) {
                            IconButton(onClick = { selectedTime = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Очистити час")
                            }
                        }
                    }
                }
            }
        }

        // Нагадування
        ReminderSection(
            reminderType = reminderType,
            reminderDate = reminderDate,
            reminderTime = reminderTime,
            reminderDateFrom = reminderDateFrom,
            reminderDateTo = reminderDateTo,
            onReminderTypeChange = { reminderType = it },
            onReminderDateChange = { reminderDate = it },
            onReminderTimeChange = { reminderTime = it },
            onReminderDateFromChange = { reminderDateFrom = it },
            onReminderDateToChange = { reminderDateTo = it },
            onClear = {
                reminderType = ""
                reminderDate = ""
                reminderTime = ""
                reminderDateFrom = ""
                reminderDateTo = ""
            }
        )

        // Топік
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
                        onValueChange = { topicText = it; showTopicDropdown = true },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        singleLine = true,
                        placeholder = { Text("Введіть або оберіть топік") },
                        trailingIcon = {
                            if (allTopicsForDropdown.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTopicDropdown)
                            }
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showTopicDropdown && filteredTopics.isNotEmpty(),
                        onDismissRequest = { showTopicDropdown = false },
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        filteredTopics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic) },
                                onClick = { topicText = topic; showTopicDropdown = false }
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

        // Опис зі скріпкою
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Опис", modifier = Modifier.width(100.dp).padding(top = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 40.dp),
                        minLines = 3
                    )
                    var showMenu by remember { mutableStateOf(false) }
                    val imagePicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetMultipleContents()
                    ) { uris -> attachments = (attachments + uris.map { it.toString() }).take(10) }
                    val filePicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetMultipleContents()
                    ) { uris -> attachments = (attachments + uris.map { it.toString() }).take(10) }

                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Прикріпити")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Додати зображення") },
                                onClick = { showMenu = false; imagePicker.launch("image/*") }
                            )
                            DropdownMenuItem(
                                text = { Text("Додати файл") },
                                onClick = { showMenu = false; filePicker.launch("*/*") }
                            )
                        }
                    }
                }

                attachments.forEachIndexed { index, path ->
                    val isImage = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
                        .any { path.lowercase().endsWith(it) } || path.contains("image")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isImage) {
                            AsyncImage(
                                model = path,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).padding(8.dp)
                            )
                        }
                        Text(
                            text = path.substringAfterLast("/"),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            maxLines = 1
                        )
                        IconButton(onClick = {
                            attachments = attachments.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Видалити")
                        }
                    }
                }
            }
        }

        // Підтаски
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Підтаски",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        subtasksFromDb.forEach { subtask ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                        .clickable { navController.navigate("edit_subtask/${subtask.id}") },
                    style = if (subtask.done) {
                        MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    }
                )
                IconButton(onClick = { navController.navigate("edit_subtask/${subtask.id}") }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Редагувати", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                    scope.launch { App.db.subtaskDao().delete(subtask) }
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Видалити", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        OutlinedButton(
            onClick = { navController.navigate("add_subtask/$noteId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("+ Додати підтаску")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (noteText.isNotBlank()) {
                    scope.launch {
                        if (topicText.isNotBlank()) ensureTopicExists(topicText)
                        App.db.noteDao().update(
                            NoteEntity(
                                id = noteId,
                                text = noteText,
                                emoji = selectedEmoji,
                                date = selectedDate,
                                time = selectedTime,
                                topic = topicText,
                                description = description,
                                attachments = encodeAttachments(attachments),
                                reminderType = reminderType,
                                reminderDate = reminderDate,
                                reminderTime = reminderTime,
                                reminderDateFrom = reminderDateFrom,
                                reminderDateTo = reminderDateTo
                            )
                        )
                        cancelReminder(context, noteId)
                        when (reminderType) {
                            "single" -> if (reminderDate.isNotEmpty() && reminderTime.isNotEmpty()) {
                                scheduleReminder(context, noteId, noteText, reminderDate, reminderTime)
                            }
                            "range" -> if (reminderDateFrom.isNotEmpty() && reminderDateTo.isNotEmpty() && reminderTime.isNotEmpty()) {
                                scheduleRangeReminders(context, noteId, noteText, reminderDateFrom, reminderDateTo, reminderTime)
                            }
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
