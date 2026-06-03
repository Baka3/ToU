package com.example.tou

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(navController: NavController) {
    val notes by App.db.noteDao().getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var isWeekView by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    // збираємо всі дати з термінами і нагадуваннями
    val datesWithEvents = remember(notes) {
        notes.mapNotNull { note ->
            val dates = mutableListOf<LocalDate>()
            if (note.date.isNotEmpty()) {
                runCatching {
                    LocalDate.parse(note.date, formatter)
                }.getOrNull()?.let { dates.add(it) }
            }
            if (note.reminderDate.isNotEmpty()) {
                runCatching {
                    LocalDate.parse(note.reminderDate, formatter)
                }.getOrNull()?.let { dates.add(it) }
            }
            dates
        }.flatten().toSet()
    }

    // нотатки для обраної дати
    val notesForSelectedDate = remember(notes, selectedDate) {
        val dateStr = selectedDate.format(formatter)
        notes.filter { note ->
            note.date == dateStr ||
                    note.reminderDate == dateStr ||
                    (note.reminderType == "range" &&
                            note.reminderDateFrom.isNotEmpty() &&
                            note.reminderDateTo.isNotEmpty() &&
                            runCatching {
                                val from = LocalDate.parse(note.reminderDateFrom, formatter)
                                val to = LocalDate.parse(note.reminderDateTo, formatter)
                                !selectedDate.isBefore(from) && !selectedDate.isAfter(to)
                            }.getOrDefault(false))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Перемикач вигляду
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !isWeekView,
                onClick = { isWeekView = false },
                label = { Text("Місяць") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = isWeekView,
                onClick = { isWeekView = true },
                label = { Text("Тиждень") }
            )
        }

        if (isWeekView) {
            // Тижневий вигляд
            val weekState = rememberWeekCalendarState(
                startDate = LocalDate.now().minusWeeks(52),
                endDate = LocalDate.now().plusWeeks(52),
                firstVisibleWeekDate = selectedDate,
                firstDayOfWeek = DayOfWeek.MONDAY
            )

            WeekCalendar(
                state = weekState,
                dayContent = { day ->
                    CalendarDay(
                        day = day.date,
                        isSelected = day.date == selectedDate,
                        hasEvents = day.date in datesWithEvents,
                        onClick = { selectedDate = day.date }
                    )
                }
            )
        } else {
            // Місячний вигляд
            val currentMonth = remember { YearMonth.now() }
            val monthState = rememberCalendarState(
                startMonth = currentMonth.minusMonths(12),
                endMonth = currentMonth.plusMonths(12),
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = DayOfWeek.MONDAY
            )

            // Заголовок місяця
            val visibleMonth = monthState.firstVisibleMonth.yearMonth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    scope.launch {
                        monthState.animateScrollToMonth(visibleMonth.minusMonths(1))
                    }
                }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                }
                Text(
                    text = visibleMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("uk"))
                        .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = {
                    scope.launch {
                        monthState.animateScrollToMonth(visibleMonth.plusMonths(1))
                    }
                }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }

            // Дні тижня
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalCalendar(
                state = monthState,
                dayContent = { day ->
                    CalendarDay(
                        day = day.date,
                        isSelected = day.date == selectedDate,
                        hasEvents = day.date in datesWithEvents,
                        isCurrentMonth = day.position == DayPosition.MonthDate,
                        onClick = { selectedDate = day.date }
                    )
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Заголовок обраного дня
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.format(
                    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("uk"))
                ),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                navController.navigate("add_note_full")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Додати")
            }
        }

        // Список нотаток для обраної дати
        if (notesForSelectedDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нічого на цей день", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(notesForSelectedDate) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("edit/${note.id}") }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.emoji.isNotEmpty()) {
                            Text(
                                text = note.emoji,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            // показуємо тип події
                            val eventInfo = buildString {
                                if (note.date.isNotEmpty()) append("Термін: ${note.date}")
                                if (note.reminderDate.isNotEmpty()) {
                                    if (isNotEmpty()) append(" • ")
                                    append("Нагадування: ${note.reminderDate} ${note.reminderTime}")
                                }
                                if (note.reminderType == "range" && note.reminderDateFrom.isNotEmpty()) {
                                    if (isNotEmpty()) append(" • ")
                                    append("Щодня о ${note.reminderTime}")
                                }
                            }
                            if (eventInfo.isNotEmpty()) {
                                Text(
                                    text = eventInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        if (note.time.isNotEmpty()) {
                            Text(
                                text = note.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: LocalDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit
) {
    val isToday = day == LocalDate.now()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    !isCurrentMonth -> Color.LightGray
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> Color.Unspecified
                },
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}