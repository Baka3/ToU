package com.example.tou

import android.app.DatePickerDialog
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

@Composable
fun AddNoteFullScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var noteText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("") }
    var showEmojiField by remember { mutableStateOf(false) }

    // Календар
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "$day.${month + 1}.$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Нотаточка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Нотаточка",
                modifier = Modifier.width(100.dp)
            )
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Термін
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Термін",
                modifier = Modifier.width(100.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
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
        }

        // Іконка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Іконка",
                modifier = Modifier.width(100.dp)
            )
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

        // Поле вводу емодзі
        if (showEmojiField) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = selectedEmoji,
                    onValueChange = {
                        if (it.length <= 2) selectedEmoji = it // обмежуємо до одного емодзі
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введіть емодзі") },
                    singleLine = true
                )
                TextButton(onClick = { showEmojiField = false }) {
                    Text("Ок")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка підтаски
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати підтаски")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка зберегти
        Button(
            onClick = {
                if (noteText.isNotBlank()) {
                    scope.launch {
                        App.db.noteDao().insert(NoteEntity(text = noteText))
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Зберегти")
        }
    }
}