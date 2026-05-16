package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EditSubtaskScreen(navController: NavController, subtaskId: Int) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    LaunchedEffect(subtaskId) {
        val subtask = App.db.subtaskDao().getById(subtaskId)
        title = subtask?.title ?: ""
        description = subtask?.description ?: ""
        selectedDate = subtask?.date ?: ""
        selectedTime = subtask?.time ?: ""
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
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Редагування підтаски",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Назва
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Назва", modifier = Modifier.width(100.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Опис
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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    scope.launch {
                        val existing = App.db.subtaskDao().getById(subtaskId)
                        if (existing != null) {
                            App.db.subtaskDao().update(
                                existing.copy(
                                    title = title,
                                    description = description,
                                    date = selectedDate,
                                    time = selectedTime
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