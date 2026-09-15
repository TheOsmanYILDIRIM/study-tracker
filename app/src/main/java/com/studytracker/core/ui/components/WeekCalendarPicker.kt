package com.studytracker.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.studytracker.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val FuturisticCardShape = RoundedCornerShape(26.dp)
private val FuturisticPillShape = CircleShape

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
        shape = FuturisticCardShape,
        colors = CardDefaults.cardColors(containerColor = ZomoDarkSurface),
        border = BorderStroke(1.dp, ZomoDarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick preset pill chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isCurrent = selectedWeekId == currentWeekInfo.first
                Surface(
                    onClick = { onWeekSelected(currentWeekInfo.first, currentWeekInfo.second) },
                    shape = FuturisticPillShape,
                    color = if (isCurrent) ZomoPurplePrimary else Color(0x28A855F7),
                    border = BorderStroke(1.dp, if (isCurrent) ZomoPurplePrimary else ZomoDarkBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Bu Hafta (${currentWeekInfo.first})",
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium,
                            color = if (isCurrent) Color.White else ZomoPurpleLight
                        )
                    }
                }

                val isNext = selectedWeekId == nextWeekInfo.first
                Surface(
                    onClick = { onWeekSelected(nextWeekInfo.first, nextWeekInfo.second) },
                    shape = FuturisticPillShape,
                    color = if (isNext) ZomoPurplePrimary else Color(0x28A855F7),
                    border = BorderStroke(1.dp, if (isNext) ZomoPurplePrimary else ZomoDarkBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Gelecek Hafta",
                            fontSize = 12.sp,
                            fontWeight = if (isNext) FontWeight.Black else FontWeight.Medium,
                            color = if (isNext) Color.White else ZomoPurpleLight
                        )
                    }
                }
            }

            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        val newCal = displayedMonthCal.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        displayedMonthCal = newCal
                    },
                    shape = FuturisticPillShape,
                    color = Color(0x28A855F7),
                    border = BorderStroke(1.dp, ZomoDarkBorder),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Ay", tint = ZomoPurpleLight)
                    }
                }

                Text(
                    text = monthYearFormat.format(displayedMonthCal.time).replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = ZomoTextPrimary
                )

                Surface(
                    onClick = {
                        val newCal = displayedMonthCal.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        displayedMonthCal = newCal
                    },
                    shape = FuturisticPillShape,
                    color = Color(0x28A855F7),
                    border = BorderStroke(1.dp, ZomoDarkBorder),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Ay", tint = ZomoPurpleLight)
                    }
                }
            }

            // Days of Week Header
            val dayHeaders = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (header in dayHeaders) {
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZomoTextSecondary
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelectedWeek) ZomoVioletContainer else Color.Transparent)
                            .border(
                                width = if (isSelectedWeek) 1.5.dp else 0.dp,
                                color = if (isSelectedWeek) ZomoPurplePrimary else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
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
                                    fontWeight = if (isSelectedWeek) FontWeight.Black else FontWeight.Normal,
                                    color = if (isSelectedWeek) ZomoNeonMint
                                    else if (isCurrentMonth) ZomoTextPrimary
                                    else ZomoTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Selected Week Banner Info (Hero Gradient Pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ZomoHeroGradient)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = FuturisticPillShape,
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = ZomoNeonMint, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Seçili Hafta: $selectedWeekId",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Pazartesi Başlangıç: $selectedStartDate",
                            fontSize = 11.sp,
                            color = ZomoNeonMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
