package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

data class SubtaskDraft(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val expanded: Boolean = true
)

@Composable
fun SubtasksSection(
    subtasks: List<SubtaskDraft>,
    onSubtasksChange: (List<SubtaskDraft>) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.fillMaxWidth()) {

        // список підтасок
        subtasks.forEachIndexed { index, subtask ->
            val isExpanded = subtask.expanded

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Заголовок підтаски з кнопками згорнути/розгорнути і видалити
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtask.title.ifEmpty { "Підтаска ${index + 1}" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(onClick = {
                        onSubtasksChange(subtasks.mapIndexed { i, s ->
                            if (i == index) s.copy(expanded = !s.expanded) else s
                        })
                    }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Згорнути" else "Розгорнути"
                        )
                    }
                    IconButton(onClick = {
                        onSubtasksChange(subtasks.filterIndexed { i, _ -> i != index })
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Видалити",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Поля підтаски — розгортаються
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        // Назва
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Назва", modifier = Modifier.width(80.dp))
                            TextField(
                                value = subtask.title,
                                onValueChange = { newVal ->
                                    onSubtasksChange(subtasks.mapIndexed { i, s ->
                                        if (i == index) s.copy(title = newVal) else s
                                    })
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Опис
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Опис", modifier = Modifier.width(80.dp))
                            TextField(
                                value = subtask.description,
                                onValueChange = { newVal ->
                                    onSubtasksChange(subtasks.mapIndexed { i, s ->
                                        if (i == index) s.copy(description = newVal) else s
                                    })
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Термін
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                onSubtasksChange(subtasks.mapIndexed { i, s ->
                                    if (i == index) s.copy(date = "$day.${month + 1}.$year") else s
                                })
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                onSubtasksChange(subtasks.mapIndexed { i, s ->
                                    if (i == index) s.copy(time = "%02d:%02d".format(hour, minute)) else s
                                })
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Термін", modifier = Modifier.width(80.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth().height(48.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                        .clickable { datePickerDialog.show() },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = subtask.date.ifEmpty { "Оберіть дату" },
                                        modifier = Modifier.padding(start = 12.dp),
                                        color = if (subtask.date.isEmpty()) Color.Gray else Color.Unspecified
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth().height(48.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                        .clickable { timePickerDialog.show() },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = subtask.time.ifEmpty { "Оберіть час" },
                                        modifier = Modifier.padding(start = 12.dp),
                                        color = if (subtask.time.isEmpty()) Color.Gray else Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Кнопка "Додати підтаску"
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onSubtasksChange(subtasks + SubtaskDraft()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Додати підтаску")
        }
    }
}