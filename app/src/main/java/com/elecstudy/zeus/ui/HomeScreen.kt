package com.elecstudy.zeus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.elecstudy.zeus.model.Post
import com.elecstudy.zeus.ui.theme.*
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<Post> = emptyList(),
    onPostClick: (Post) -> Unit
) {
    val today = LocalDate.now()
    val examDate = LocalDate.of(2026, 4, 25)
    val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, examDate)

    var selectedDate by remember { mutableStateOf(today) }

    val calendarState = rememberCalendarState(
        startMonth = YearMonth.of(2023, 1),
        endMonth = YearMonth.of(2026, 12),
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    val koreanWeekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZeusDark)
            .padding(16.dp)
    ) {
        // D-Day Section
        Card(
            colors = CardDefaults.cardColors(containerColor = ZeusCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚡ 시험까지",
                    color = ZeusTextLight,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "D-$daysLeft",
                    color = ZeusElectric,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = examDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                    color = ZeusTextLight.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        Card(
            colors = CardDefaults.cardColors(containerColor = ZeusDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = calendarState.firstVisibleMonth.yearMonth.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
                        color = ZeusElectric,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // Days of Week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    koreanWeekDays.forEach { day ->
                        Text(
                            text = day,
                            color = ZeusTextLight.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Grid
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        val isSelected = day.date == selectedDate
                        val isToday = day.date == today
                        val isCurrentMonth = day.position == DayPosition.MonthDate

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f) // Make cells square
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    color = when {
                                        isSelected -> ZeusElectric
                                        isToday -> ZeusElectric.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = isCurrentMonth) {
                                    if (isCurrentMonth) selectedDate = day.date
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentMonth) {
                                Text(
                                    text = day.date.dayOfMonth.toString(),
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) ZeusDark else ZeusTextLight,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}







