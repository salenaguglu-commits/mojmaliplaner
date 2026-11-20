package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.TaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    tasks: List<TaskEntity>,
    onAddStudyTask: (title: String, subjectName: String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedSubjectForHomework by remember { mutableStateOf<String?>(null) }
    var homeworkTitle by remember { mutableStateOf("") }
    val context = LocalContext.current

    val isSerbia = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    val timetable = if (isSerbia) {
        mapOf(
            "Ponedeljak" to listOf("Francuski 🇫🇷", "Srpski 🇷🇸", "Fizičko 🏃", "Geografija 🌍", "Istorija 📜", "Matematika 📐"),
            "Utorak" to listOf("Engleski 🇬🇧", "Matematika 📐", "ČOS 👥", "Srpski 🇷🇸", "Fizika ⚡"),
            "Sreda" to listOf("Fizičko 🏃", "Biologija 🔬", "Geografija 🌍", "Likovno 🎨", "Srpski 🇷🇸", "Matematika 📐"),
            "Četvrtak" to listOf("Informatika 💻", "Matematika 📐", "Francuski 🇫🇷", "Engleski 🇬🇧", "Muzičko 🎵", "Fizika ⚡"),
            "Petak" to listOf("Građansko 🕊️", "Srpski 🇷🇸", "Fizičko 🏃", "Istorija 📜", "Biologija 🔬", "Tehničko (blok) 🛠️")
        )
    } else {
        mapOf(
            "Monday" to listOf("French 🇫🇷", "English 🇬🇧", "PE 🏃", "Geography 🌍", "History 📜", "Math 📐"),
            "Tuesday" to listOf("English 🇬🇧", "Math 📐", "Homeroom 👥", "Science 🔬", "Physics ⚡"),
            "Wednesday" to listOf("PE 🏃", "Biology 🔬", "Geography 🌍", "Art 🎨", "English 🇬🇧", "Math 📐"),
            "Thursday" to listOf("Computer Science 💻", "Math 📐", "French 🇫🇷", "English 🇬🇧", "Music 🎵", "Physics ⚡"),
            "Friday" to listOf("Civics 🕊️", "English 🇬🇧", "PE 🏃", "History 📜", "Biology 🔬", "Technology 🛠️")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.study_topbar),
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
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text(if (isSerbia) "Raspored časova 🏫" else "School Timetable 🏫", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(if (isSerbia) "Domaći i Kontrolni 📝" else "Homework & Exams 📝", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = if (isSerbia) "💡 Klikni na bilo koji predmet iz rasporeda da dodaš domaći zadatak ili podsetnik!" else "💡 Tap any subject in the schedule to add homework or a reminder!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    items(timetable.entries.toList()) { (day, subjects) ->
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
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    subjects.forEachIndexed { index, subject ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSubjectForHomework = subject
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${index + 1}. $subject",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Dodaj domaći",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val studyTasks = tasks.filter { it.category == "ŠKOLA" }

                    if (studyTasks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = if (isSerbia) "Trenutno nema zadataka za školu! Klikni na raspored da dodaš novi zadatak. ✨" else "No school assignments right now! Tap a subject in the schedule to add one. ✨",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(studyTasks, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                .clickable { onToggleTask(task) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (task.isCompleted) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Završeno",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            if (!task.subjectName.isNullOrBlank()) {
                                                Text(
                                                    text = task.subjectName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onDeleteTask(task) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Obriši",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
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

    if (selectedSubjectForHomework != null) {
        Dialog(onDismissRequest = { selectedSubjectForHomework = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSerbia) "Novi zadatak za ${selectedSubjectForHomework!!}" else "New assignment for ${selectedSubjectForHomework!!}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { selectedSubjectForHomework = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    OutlinedTextField(
                        value = homeworkTitle,
                        onValueChange = { homeworkTitle = it },
                        placeholder = { Text(if (isSerbia) "Npr. Uradi stranu 45, vežba 3..." else "E.g. Page 45, exercise 3...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Button(
                        onClick = {
                            if (homeworkTitle.isNotBlank()) {
                                onAddStudyTask(homeworkTitle, selectedSubjectForHomework!!)
                                Toast.makeText(context, "Zadatak sačuvan za ${selectedSubjectForHomework!!} 📚", Toast.LENGTH_SHORT).show()
                                homeworkTitle = ""
                                selectedSubjectForHomework = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Sačuvaj zadatak ✨", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
