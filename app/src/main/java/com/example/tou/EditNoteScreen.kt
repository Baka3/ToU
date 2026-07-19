package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Undo

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
    var reminderState by remember { mutableStateOf(ReminderState()) }
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

    var showDoodle by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var showViewer by remember { mutableStateOf(false) }
    var selectedViewerIndex by remember { mutableStateOf(0) }
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

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
        if (uris.isNotEmpty()) {
            attachments = (attachments + uris.map { it.toString() }).take(10)
            selectedViewerIndex = attachments.size - uris.size
            showViewer = true
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        attachments = (attachments + uris.map { it.toString() }).take(10)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        ScreenHeader(title = stringResource(R.string.screen_edit_note), navController = navController)

        // Нотаточка
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text( text = stringResource(R.string.nav_notes), modifier = Modifier.width(100.dp))
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 5
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

        // Нагадування
        ReminderSection(
            state = reminderState,
            onChange = { reminderState = it },
            onClear = { reminderState = ReminderState() }
        )

        // Топік
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
                        onValueChange = { topicText = it; showTopicDropdown = true },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.placeholder_topic)) },
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
            Text(text = stringResource(R.string.field_icon), modifier = Modifier.width(100.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (selectedEmoji.isNotEmpty()) {
                    Text(text = selectedEmoji, fontSize = 32.sp, modifier = Modifier.padding(end = 8.dp))
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
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
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
                    val isImage = isImagePath(context, path)
                    val isVideo = isVideoPath(context, path)
                    val isAudio = isAudioPath(context, path)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedViewerIndex = index
                                showViewer = true
                            }
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
                        } else if (isAudio) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
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

        // Підтаски
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.field_subtasks),
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
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                    scope.launch { App.db.subtaskDao().delete(subtask) }
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete), modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        OutlinedButton(
            onClick = { navController.navigate("add_subtask/$noteId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.btn_add_subtask))
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
                                reminderDateTo = reminderDateTo,
                                reminderDates = encodeAttachments(reminderState.dates),
                                reminderTimes = encodeAttachments(reminderState.times),
                                reminderRepeatType = reminderState.repeatType,
                                reminderRepeatCount = reminderState.repeatCount,
                                reminderRepeatEveryHours = reminderState.repeatEveryHours,
                                reminderUntilDate = reminderState.untilDate,
                                reminderEndOfDay = reminderState.endOfDay
                            )
                        )
                        cancelReminder(context, noteId)
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
                                scheduleRangeReminders(context, noteId, noteText, reminderDateFrom, reminderDateTo, reminderTime)
                            }
                        }
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_save))
        }
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
}
