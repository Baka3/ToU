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
import java.util.Calendar
import androidx.compose.material.icons.filled.Close

@Composable
fun ReminderSection(
    reminderType: String,
    reminderDate: String,
    reminderTime: String,
    reminderDateFrom: String,
    reminderDateTo: String,
    onReminderTypeChange: (String) -> Unit,
    onReminderDateChange: (String) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onReminderDateFromChange: (String) -> Unit,
    onReminderDateToChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun makeDatePicker(onPick: (String) -> Unit) = DatePickerDialog(
        context,
        { _, y, m, d -> onPick("$d.${m + 1}.$y") },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun makeTimePicker(onPick: (String) -> Unit) = TimePickerDialog(
        context,
        { _, h, m -> onPick("%02d:%02d".format(h, m)) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Заголовок з кнопкою очистити
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //ScreenHeader(title = "Нагадування", navController = navController)
            Text(
                text = "Нагадування",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            if (reminderType.isNotEmpty()) {
                TextButton(onClick = { onClear() }) {
                    Text("Очистити", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Одне нагадування
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = reminderType == "single",
                onCheckedChange = { onReminderTypeChange(if (it) "single" else "") }
            )
            Text("Одне нагадування")
        }

        if (reminderType == "single") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { makeDatePicker(onReminderDateChange).show() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = reminderDate.ifEmpty { "Дата" },
                        modifier = Modifier.padding(start = 12.dp),
                        color = if (reminderDate.isEmpty()) Color.Gray else Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { makeTimePicker(onReminderTimeChange).show() },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = reminderTime.ifEmpty { "Час" },
                        modifier = Modifier.padding(start = 12.dp),
                        color = if (reminderTime.isEmpty()) Color.Gray else Color.Unspecified
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Проміжок
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = reminderType == "range",
                onCheckedChange = { onReminderTypeChange(if (it) "range" else "") }
            )
            Text("Проміжок")
        }

        if (reminderType == "range") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Від", modifier = Modifier.width(40.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f).height(48.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .clickable { makeDatePicker(onReminderDateFromChange).show() },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = reminderDateFrom.ifEmpty { "Дата початку" },
                            modifier = Modifier.padding(start = 12.dp),
                            color = if (reminderDateFrom.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "До", modifier = Modifier.width(40.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f).height(48.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .clickable { makeDatePicker(onReminderDateToChange).show() },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = reminderDateTo.ifEmpty { "Дата кінця" },
                            modifier = Modifier.padding(start = 12.dp),
                            color = if (reminderDateTo.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Час", modifier = Modifier.width(40.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f).height(48.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .clickable { makeTimePicker(onReminderTimeChange).show() },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = reminderTime.ifEmpty { "Час нагадування" },
                            modifier = Modifier.padding(start = 12.dp),
                            color = if (reminderTime.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}