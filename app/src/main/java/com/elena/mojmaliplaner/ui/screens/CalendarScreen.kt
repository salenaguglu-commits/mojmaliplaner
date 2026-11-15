package com.elena.mojmaliplaner.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

fun LocalDate.toMillis(): Long = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun Long.toLocalDate(): LocalDate = java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, category: String, colorHex: String, dateMillis: Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isMonthlyView by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.calendar_topbar),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = isMonthlyView,
                    onClick = { isMonthlyView = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.tab_monthly_grid), fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = !isMonthlyView,
                    onClick = { isMonthlyView = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.tab_weekly_agenda), fontWeight = FontWeight.SemiBold)
                }
            }

            if (isMonthlyView) {
                MonthlyCalendarView(
                    tasks = tasks,
                    onAddTask = onAddTask,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask
                )
            } else {
                WeeklyAgendaView(
                    tasks = tasks,
                    onAddTask = onAddTask,
                    onToggleTask = onToggleTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}

@Composable
fun MonthlyCalendarView(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, category: String, colorHex: String, dateMillis: Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("OBAVEZA") }
    var selectedColorHex by remember { mutableStateOf("#6EC5E9") }

    val presetColors = listOf("#6EC5E9", "#C49ED4", "#CBA84E", "#8A8E94", "#EAA98B")
    
    val currentLocale = Locale.getDefault()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", currentLocale)
    val monthName = currentMonth.format(monthFormatter)
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // Monday = 1, Sunday = 7
    
    // Calculate empty prefix cells (0 for Monday, 6 for Sunday)
    val emptyPrefixCount = firstDayOfWeek - 1

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(currentLocale) else it.toString() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prethodni mesec")
                }
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sledeći mesec")
                }
            }
        }

        // Month Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                val dayHeaderLabels = listOf("P", "U", "S", "Č", "P", "S", "N")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dayHeaderLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalCells = emptyPrefixCount + daysInMonth
                val rowsCount = (totalCells + 6) / 7
                
                for (r in 0 until rowsCount) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - emptyPrefixCount + 1
                            
                            if (dayNum in 1..daysInMonth) {
                                val dateOfCell = currentMonth.atDay(dayNum)
                                val isSelected = dateOfCell == selectedDate
                                val dayTasks = tasks.filter { it.dateMillis.toLocalDate() == dateOfCell }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable { 
                                            selectedDate = dateOfCell 
                                            currentMonth = YearMonth.from(dateOfCell)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        if (dayTasks.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                dayTasks.take(3).forEach { t ->
                                                    val color = try { Color(android.graphics.Color.parseColor(t.colorHex)) } catch (e: Exception) { PastelBlue }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }
        }

        // Task Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_add_task) + " - " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.")),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text(stringResource(R.string.placeholder_task_title)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                onAddTask(newTaskTitle, selectedCategory, selectedColorHex, selectedDate.toMillis())
                                newTaskTitle = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                    }
                }

                // Color Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Boja oznake:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presetColors.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = hex == selectedColorHex
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            }
        }

        // Tasks List
        val selectedDayTasks = tasks.filter { it.dateMillis.toLocalDate() == selectedDate }
        
        Text(
            text = "Obaveze za " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.")) + " 📋",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedDayTasks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.empty_tasks_day),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            } else {
                items(selectedDayTasks, key = { it.id }) { task ->
                    TaskRowCard(
                        task = task,
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyAgendaView(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, category: String, colorHex: String, dateMillis: Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    val currentLocale = Locale.getDefault()
    var currentWeekStart by remember { mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))) }
    
    val weekDays = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    val formatter = DateTimeFormatter.ofPattern("EEEE", currentLocale)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nedeljni pregled 📆",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prethodna nedelja")
                }
                IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sledeća nedelja")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(weekDays) { loopDate ->
                val dayName = loopDate.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(currentLocale) else it.toString() }
                val dayNumber = loopDate.dayOfMonth
                val isToday = loopDate == LocalDate.now()
                
                var dayTaskInput by remember { mutableStateOf("") }
                val dayIndex = loopDate.dayOfWeek.value - 1 // 0 for Monday

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (dayIndex % 2 == 0) PastelBlue else PastelPurple)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Day Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = if (dayIndex % 2 == 0) PastelBlue.copy(alpha = 0.25f) else PastelPurple.copy(alpha = 0.25f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$dayNumber",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Text(
                                    text = "$dayName, $dayNumber.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (isToday) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Danas ⭐",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Quick task input row for this specific day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dayTaskInput,
                                onValueChange = { dayTaskInput = it },
                                placeholder = { Text(stringResource(R.string.placeholder_task_title)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Button(
                                onClick = {
                                    if (dayTaskInput.isNotBlank()) {
                                        onAddTask(dayTaskInput, "OBAVEZA", if (dayIndex % 2 == 0) "#6EC5E9" else "#C49ED4", loopDate.toMillis())
                                        dayTaskInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                            }
                        }

                        // List tasks for this day or empty state
                        val dayTasks = tasks.filter { it.dateMillis.toLocalDate() == loopDate }

                        if (dayTasks.isEmpty()) {
                            Text(
                                text = stringResource(R.string.empty_tasks_day),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dayTasks.forEach { task ->
                                    TaskRowCard(
                                        task = task,
                                        onToggleTask = onToggleTask,
                                        onDeleteTask = onDeleteTask
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRowCard(
    task: TaskEntity,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            try {
                Color(android.graphics.Color.parseColor(task.colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onToggleTask(task) },
                    shape = CircleShape,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Završeno",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = { onDeleteTask(task) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Obriši",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
