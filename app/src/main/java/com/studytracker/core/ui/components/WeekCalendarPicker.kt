package com.studytracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
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
import com.studytracker.core.ui.theme.SapphirePrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeekCalendarPicker(
    selectedWeekId: String,
    selectedStartDate: String,
    onWeekSelected: (weekId: String, weekStartDate: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localeTr = remember { Locale("tr", "TR") }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", localeTr) }
    val shortDateFormat = remember { SimpleDateFormat("d MMM", localeTr) }

    // Calendar state for month navigation
    var displayedMonthCal by remember {
        val cal = Calendar.getInstance(Locale.US)
        if (selectedStartDate.isNotBlank()) {
            try {
                cal.time = dateFormat.parse(selectedStartDate) ?: Date()
            } catch (_: Exception) {}
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    // Helper to calculate week info from a date
    fun getWeekInfo(cal: Calendar): Pair<String, String> {
        val clone = cal.clone() as Calendar
        clone.firstDayOfWeek = Calendar.MONDAY
        clone.minimalDaysInFirstWeek = 4

        val dayOfWeek = clone.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        clone.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

        val year = clone.get(Calendar.YEAR)
        val week = clone.get(Calendar.WEEK_OF_YEAR)
        val weekId = String.format(Locale.US, "%04d-W%02d", year, week)
        val startDate = dateFormat.format(clone.time)
        return Pair(weekId, startDate)
    }

    // Quick helpers
    val currentWeekInfo = remember {
        getWeekInfo(Calendar.getInstance(Locale.US))
    }
    val nextWeekInfo = remember {
        val c = Calendar.getInstance(Locale.US)
        c.add(Calendar.WEEK_OF_YEAR, 1)
        getWeekInfo(c)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quick preset chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedWeekId == currentWeekInfo.first,
                    onClick = { onWeekSelected(currentWeekInfo.first, currentWeekInfo.second) },
                    label = { Text("Bu Hafta (${currentWeekInfo.first})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedWeekId == nextWeekInfo.first,
                    onClick = { onWeekSelected(nextWeekInfo.first, nextWeekInfo.second) },
                    label = { Text("Gelecek Hafta", fontSize = 12.sp) }
                )
            }

            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newCal = displayedMonthCal.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        displayedMonthCal = newCal
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Ay")
                }

                Text(
                    text = monthYearFormat.format(displayedMonthCal.time).replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        val newCal = displayedMonthCal.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        displayedMonthCal = newCal
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Ay")
                }
            }

            // Days of Week Header (Pzt, Sal, Çar, Per, Cum, Cmt, Paz)
            val dayHeaders = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (header in dayHeaders) {
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calendar Grid by Weeks
            val weeksInMonth = remember(displayedMonthCal) {
                val weeks = mutableListOf<List<Calendar>>()
                val cal = displayedMonthCal.clone() as Calendar
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.firstDayOfWeek = Calendar.MONDAY

                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

                val targetMonth = displayedMonthCal.get(Calendar.MONTH)

                var finished = false
                while (!finished) {
                    val weekDays = mutableListOf<Calendar>()
                    for (i in 0 until 7) {
                        weekDays.add(cal.clone() as Calendar)
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    weeks.add(weekDays)
                    if (weekDays.last().get(Calendar.MONTH) != targetMonth && weekDays.first().get(Calendar.MONTH) != targetMonth) {
                        finished = true
                    }
                    if (weeks.size >= 6) finished = true
                }
                weeks
            }

            // Render Weeks
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekDays in weeksInMonth) {
                    val mondayCal = weekDays.first()
                    val (weekId, startDate) = getWeekInfo(mondayCal)
                    val isSelectedWeek = weekId == selectedWeekId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelectedWeek) SapphirePrimary.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSelectedWeek) 1.5.dp else 0.dp,
                                color = if (isSelectedWeek) SapphirePrimary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onWeekSelected(weekId, startDate)
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (dayCal in weekDays) {
                            val isCurrentMonth = dayCal.get(Calendar.MONTH) == displayedMonthCal.get(Calendar.MONTH)
                            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelectedWeek) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelectedWeek) SapphirePrimary
                                    else if (isCurrentMonth) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // Selected Week Banner Info
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            text = "Seçili Hafta: $selectedWeekId",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Pazartesi Başlangıç: $selectedStartDate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
