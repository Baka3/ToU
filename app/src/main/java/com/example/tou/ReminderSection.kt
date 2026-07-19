package com.example.tou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

data class ReminderState(
    val dates: List<String> = emptyList(),
    val times: List<String> = emptyList(),
    val repeatType: String = "",
    val repeatCount: Int = 1,
    val repeatEveryHours: Int = 1,
    val untilDate: String = "",
    val endOfDay: Boolean = false
)

@Composable
fun ReminderSection(
    state: ReminderState,
    onChange: (ReminderState) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var showRepeatOptions by remember { mutableStateOf(false) }
    var showFrequencyOptions by remember { mutableStateOf(false) }

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

    // перевіряємо чи нагадування встигне до кінця дня
    fun checkEndOfDayWarning(): Boolean {
        if (!state.endOfDay || state.times.isEmpty()) return false
        val lastTime = state.times.last()
        val timeParts = lastTime.split(":")
        val hour = timeParts[0].toIntOrNull() ?: 0
        val minute = timeParts[1].toIntOrNull() ?: 0
        val minutesFromMidnight = hour * 60 + minute
        return minutesFromMidnight > 23 * 60
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Нагадування",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            if (state.dates.isNotEmpty() || state.times.isNotEmpty()) {
                TextButton(onClick = { onClear() }) {
                    Text("Очистити", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // БЛОК ДАТИ
        Text(
            text = "Дата:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        // список обраних дат
        state.dates.forEachIndexed { index, date ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = date, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    onChange(state.copy(dates = state.dates.toMutableList().also { it.removeAt(index) }))
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // кнопка додати дату
        OutlinedButton(
            onClick = { makeDatePicker { date ->
                onChange(state.copy(dates = state.dates + date))
            }.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(" дата")
        }

        // Налаштувати періодичність
        if (state.dates.isNotEmpty()) {
            TextButton(
                onClick = { showRepeatOptions = !showRepeatOptions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showRepeatOptions) "▲ Сховати періодичність" else "▼ Налаштувати періодичність",
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(visible = showRepeatOptions) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    listOf(
                        "daily" to "Кожного дня",
                        "weekly" to "Кожного тижня",
                        "yearly" to "Кожного року",
                        "custom" to "Обрати дати"
                    ).forEach { (type, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChange(state.copy(repeatType = if (state.repeatType == type) "" else type)) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.repeatType == type,
                                onClick = { onChange(state.copy(repeatType = if (state.repeatType == type) "" else type)) }
                            )
                            Text(text = label)
                        }
                    }

                    if (state.repeatType == "custom") {
                        OutlinedButton(
                            onClick = { makeDatePicker { date ->
                                if (date !in state.dates) {
                                    onChange(state.copy(dates = state.dates + date))
                                }
                            }.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Додати ще дату")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // БЛОК ЧАСУ
        Text(
            text = "Час:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // список обраних часів
        state.times.forEachIndexed { index, time ->
            val isOverdue = run {
                val parts = time.split(":")
                val h = parts[0].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 0
                state.endOfDay && (h * 60 + m) > 23 * 60
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time,
                    modifier = Modifier.weight(1f),
                    color = if (isOverdue) Color.Red else Color.Unspecified,
                    textDecoration = if (isOverdue) TextDecoration.Underline else TextDecoration.None
                )
                IconButton(onClick = {
                    onChange(state.copy(times = state.times.toMutableList().also { it.removeAt(index) }))
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            if (isOverdue) {
                Text(
                    text = "⚠ Нагадування не встигне прийти до кінця дня",
                    color = Color.Red,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }

        // кнопка додати час
        OutlinedButton(
            onClick = { makeTimePicker { time ->
                onChange(state.copy(times = state.times + time))
            }.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(" час")
        }

        // Налаштувати частоту
        if (state.times.isNotEmpty()) {
            TextButton(
                onClick = { showFrequencyOptions = !showFrequencyOptions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showFrequencyOptions) "▲ Сховати частоту" else "▼ Налаштувати частоту",
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(visible = showFrequencyOptions) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    // Повторювати N разів
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Повторювати", modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            if (state.repeatCount > 1)
                                onChange(state.copy(repeatCount = state.repeatCount - 1))
                        }) { Text("-") }
                        Text(text = "${state.repeatCount}", modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = {
                            onChange(state.copy(repeatCount = state.repeatCount + 1))
                        }) { Text("+") }
                        Text(text = "разів")
                    }

                    // Кожні N годин
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Кожні", modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            if (state.repeatEveryHours > 1)
                                onChange(state.copy(repeatEveryHours = state.repeatEveryHours - 1))
                        }) { Text("-") }
                        Text(text = "${state.repeatEveryHours}", modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = {
                            onChange(state.copy(repeatEveryHours = state.repeatEveryHours + 1))
                        }) { Text("+") }
                        Text(text = "годин")
                    }

                    // До якої дати
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "До", modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .clickable { makeDatePicker { onChange(state.copy(untilDate = it)) }.show() }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.untilDate.ifEmpty { "оберіть дату" }, color = if (state.untilDate.isEmpty()) Color.Gray else Color.Unspecified)
                        }
                        if (state.untilDate.isNotEmpty()) {
                            IconButton(onClick = { onChange(state.copy(untilDate = "")) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // До кінця дня
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChange(state.copy(endOfDay = !state.endOfDay)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.endOfDay,
                            onCheckedChange = { onChange(state.copy(endOfDay = it)) }
                        )
                        Text(text = "Нагадати до кінця дня")
                    }
                }
            }
        }
    }
}