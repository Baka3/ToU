package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import androidx.compose.material3.AlertDialog

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

    var showAttachMenu by remember { mutableStateOf(false) }
    var showViewer by remember { mutableStateOf(false) }
    var selectedViewerIndex by remember { mutableStateOf(0) }
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDoodle by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            attachments = (attachments + cameraImageUri.toString()).take(10)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { }
        }
        attachments = (attachments + uris.map { it.toString() }).take(10)
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { }
        }
        attachments = (attachments + uris.map { it.toString() }).take(10)
    }

    if (showViewer && attachments.isNotEmpty()) {
        ImageViewerDialog(
            images = attachments,
            initialIndex = selectedViewerIndex.coerceIn(0, attachments.size - 1),
            onDismiss = { showViewer = false },
            onDelete = { index ->
                attachments = attachments.toMutableList().also { it.removeAt(index) }
                if (attachments.isEmpty()) showViewer = false
            }
        )
    }

    var isRecording by remember { mutableStateOf(false) }
    var showRecordingDialog by remember { mutableStateOf(false) }
    val voiceRecorder = remember { VoiceRecorderHelper(context) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showRecordingDialog = true
    }

    if (showDoodle) {
        DoodleScreen(
            onSave = { path ->
                attachments = (attachments + path).take(10)
                showDoodle = false
            },
            onDismiss = { showDoodle = false }
        )
    }

    if (showRecordingDialog) {
        AlertDialog(
            onDismissRequest = {
                if (isRecording) {
                    val path = voiceRecorder.stopRecording()
                    attachments = (attachments + path).take(10)
                    isRecording = false
                }
                showRecordingDialog = false
            },
            title = { Text(if (isRecording) stringResource(R.string.rec_title_recording) else stringResource(R.string.rec_title_voice)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.RadioButtonChecked
                        else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isRecording) stringResource(R.string.rec_status_stopping) else stringResource(R.string.rec_status_starting))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isRecording) {
                        val path = voiceRecorder.stopRecording()
                        attachments = (attachments + path).take(10)
                        isRecording = false
                        showRecordingDialog = false
                    } else {
                        voiceRecorder.startRecording()
                        isRecording = true
                    }
                }) {
                    Text(if (isRecording) stringResource(R.string.rec_stop) else stringResource(R.string.rec_start))
                }
            },
            dismissButton = {
                if (!isRecording) {
                    TextButton(onClick = { showRecordingDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.screen_create_note), navController = navController)
        // Нотаточка
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.nav_notes), modifier = Modifier.width(100.dp))
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(R.string.placeholder_enter_note))
                }
            )
        }

        // Термін
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.deadlines), modifier = Modifier.width(100.dp))
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
                            text = if (selectedDate.isEmpty()) stringResource(R.string.placeholder_date) else selectedDate,
                            modifier = Modifier.padding(start = 16.dp),
                            color = if (selectedDate.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                    if (selectedDate.isNotEmpty()) {
                        IconButton(onClick = { selectedDate = ""; selectedTime = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cd_delete))
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
                                text = if (selectedTime.isEmpty()) stringResource(R.string.placeholder_time) else selectedTime,
                                modifier = Modifier.padding(start = 16.dp),
                                color = if (selectedTime.isEmpty()) Color.Gray else Color.Unspecified
                            )
                        }
                        if (selectedTime.isNotEmpty()) {
                            IconButton(onClick = { selectedTime = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cd_delete))
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
                    Text(text = stringResource(R.string.field_topic), modifier = Modifier.width(100.dp))
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
                                placeholder = { Text(stringResource(R.string.placeholder_topic)) },
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
                                modifier = Modifier.heightIn(max = 200.dp)
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
                    Text(text = stringResource(R.string.field_icon), modifier = Modifier.width(100.dp))
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
                            Text(if (selectedEmoji.isEmpty()) stringResource(R.string.btn_choose_emoji) else stringResource(R.string.btn_change_emoji))
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
                            placeholder = { Text(stringResource(R.string.placeholder_emoji)) },
                            singleLine = true
                        )
                        TextButton(onClick = { showEmojiField = false }) { Text(stringResource(R.string.btn_ok)) }
                    }
                }
        // Опис
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = stringResource(R.string.field_description), modifier = Modifier.width(100.dp).padding(top = 16.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.weight(1f),
                minLines = 3,
                placeholder = {
                    Text(text = stringResource(R.string.placeholder_enter_description))
                }
            )
            // скріпка справа за полем
            Box {
                IconButton(onClick = { showAttachMenu = true }) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = stringResource(R.string.cd_attach))
                }
                DropdownMenu(
                    expanded = showAttachMenu,
                    onDismissRequest = { showAttachMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.doodle)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Draw, contentDescription = null) },
                        onClick = {
                            showAttachMenu = false
                            showDoodle = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.take_photo)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null) },
                        onClick = {
                            showAttachMenu = false
                            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                photoFile
                            )
                            cameraImageUri = uri
                            val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                            }
                            //cameraLauncher.launch(intent)
                            cameraLauncher.launch(uri)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.btn_add_image)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Image, contentDescription = null) },
                        onClick = { showAttachMenu = false; imagePicker.launch("image/*") }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rec_title_voice)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Mic, contentDescription = null) },
                        onClick = {
                            showAttachMenu = false
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.btn_add_file)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.AttachFile, contentDescription = null) },
                        onClick = { showAttachMenu = false; filePicker.launch("*/*") }
                    )
                }
            }
        }

// Прикріплені файли ЗНИЗУ поля окремо
        if (attachments.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                attachments.forEachIndexed { index, path ->
                    val isImage = isImagePath(path)
                    val isVideo = isVideoPath(path)
                    val isAudio = isAudioPath(path)

                    if (isAudio) {
                        AudioPlayerRow(
                            path = path,
                            onDelete = {
                                attachments = attachments.toMutableList().also { it.removeAt(index) }
                            }
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedViewerIndex = index; showViewer = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isImage) {
                                AsyncImage(
                                    model = path,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (isVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
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
                                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cd_delete))
                            }
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
                Text(stringResource(R.string.btn_add_subtask))
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
                            navController.navigate("edit/$parentNoteId") {
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
                                    scheduleReminder(
                                        context,
                                        noteId.toInt(),
                                        noteText,
                                        reminderDate,
                                        reminderTime,
                                        deadline = if (selectedDate.isNotEmpty()) "${selectedDate} ${selectedTime}".trim() else "",
                                        description = description
                                    )
                                }
                                "range" -> if (reminderDateFrom.isNotEmpty() && reminderDateTo.isNotEmpty() && reminderTime.isNotEmpty()) {
                                    scheduleRangeReminders(context, noteId.toInt(), noteText, reminderDateFrom, reminderDateTo, reminderTime)
                                }
                            }
                            navController.popBackStack()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_save))
        }
    }
}
