package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.AttachFile
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteFullScreen(navController: NavController, defaultTopic: String = "", parentNoteId: Int? = null) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var noteText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("") }
    var topicText by remember { mutableStateOf(defaultTopic) }
    var showEmojiField by remember { mutableStateOf(false) }
    var showTopicDropdown by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf(listOf<String>()) }

    val existingTopics by App.db.noteDao().getTopics()
        .collectAsState(initial = emptyList())

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

    var description by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var reminderType by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }
    var reminderDateFrom by remember { mutableStateOf("") }
    var reminderDateTo by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = if (parentNoteId != null) "Створення підтаски" else "Створення нотатки",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
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
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Очистити термін")
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

        // Нагадування — окремо від терміну
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedEmoji.isNotEmpty()) {
                            Text(
                                text = selectedEmoji,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
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

        // Зображення + файли
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
                        modifier = Modifier.fillMaxWidth().padding(end = 40.dp), // місце для скріпки
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
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
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

                // Прикріплені файли під полем опису
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
                Spacer(modifier = Modifier.height(8.dp))

        if (parentNoteId == null) {
            OutlinedButton(
                onClick = {
                    scope.launch { // ← прибрали if (noteText.isNotBlank())
                        if (topicText.isNotBlank()) ensureTopicExists(topicText)
                        val maxOrder = App.db.noteDao().getMaxOrder() ?: 0
                        val noteId = App.db.noteDao().insert(
                            NoteEntity(
                                text = noteText.ifBlank { "Без назви" }, // ← дефолтна назва
                                emoji = selectedEmoji,
                                date = selectedDate,
                                time = selectedTime,
                                topic = topicText,
                                description = description,
                                order = maxOrder + 1,
                                attachments = encodeAttachments(attachments),
                                reminderType = reminderType,
                                reminderDate = reminderDate,
                                reminderTime = reminderTime,
                                reminderDateFrom = reminderDateFrom,
                                reminderDateTo = reminderDateTo
                            )
                        )
                        navController.navigate("add_subtask/${noteId.toInt()}")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Додати підтаску")
            }
        }
                Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (noteText.isNotBlank()) {
                    scope.launch {
                        if (parentNoteId != null) {
                            App.db.subtaskDao().insert(
                                SubtaskEntity(
                                    parentNoteId = parentNoteId,
                                    title = noteText,
                                    description = description,
                                    date = selectedDate,
                                    time = selectedTime,
                                    emoji = selectedEmoji,
                                    topic = topicText,
                                    attachments = encodeAttachments(attachments),
                                    reminderType = reminderType,
                                    reminderDate = reminderDate,
                                    reminderTime = reminderTime,
                                    reminderDateFrom = reminderDateFrom,
                                    reminderDateTo = reminderDateTo
                                )
                            )
                            navController.navigate("edit/${parentNoteId}") { // ← замість popBackStack
                                popUpTo("notes_list") { inclusive = false }
                            }
                        } else {
                            if (topicText.isNotBlank()) ensureTopicExists(topicText)
                            val maxOrder = App.db.noteDao().getMaxOrder() ?: 0
                            val noteId = App.db.noteDao().insert(
                                NoteEntity(
                                    text = noteText,
                                    emoji = selectedEmoji,
                                    date = selectedDate,
                                    time = selectedTime,
                                    topic = topicText,
                                    description = description,
                                    order = maxOrder + 1,
                                    attachments = encodeAttachments(attachments),
                                    reminderType = reminderType,
                                    reminderDate = reminderDate,
                                    reminderTime = reminderTime,
                                    reminderDateFrom = reminderDateFrom,
                                    reminderDateTo = reminderDateTo
                                )
                            )
                            when (reminderType) {
                                "single" -> if (reminderDate.isNotEmpty() && reminderTime.isNotEmpty()) {
                                    scheduleReminder(context, noteId.toInt(), noteText, reminderDate, reminderTime)
                                }
                                "range" -> if (reminderDateFrom.isNotEmpty() && reminderDateTo.isNotEmpty() && reminderTime.isNotEmpty()) {
                                    scheduleRangeReminders(context, noteId.toInt(), noteText, reminderDateFrom, reminderDateTo, reminderTime)
                                }
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
