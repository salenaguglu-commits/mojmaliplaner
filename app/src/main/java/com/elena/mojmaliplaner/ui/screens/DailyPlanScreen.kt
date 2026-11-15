package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.ui.theme.SoftGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyPlanScreen(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, category: String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var customTaskText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val presetTemplates = listOf("Sredi sobu 🧹", "Uradi domaći 📚", "Spakuj ranac 🎒", "Popij vodu 💧")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Moj današnji plan 📝",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Quick Templates
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
                        text = "Brzi unos zadataka za danas ✨",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetTemplates.forEach { template ->
                            Surface(
                                modifier = Modifier.clickable {
                                    onAddTask(template, "DNEVNI_PLAN")
                                    Toast.makeText(context, "Dodato: $template ⭐", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Dodaj",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = template,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Custom Add Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customTaskText,
                    onValueChange = { customTaskText = it },
                    placeholder = { Text("Dodaj svoj plan za danas...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Button(
                    onClick = {
                        if (customTaskText.isNotBlank()) {
                            onAddTask(customTaskText, "DNEVNI_PLAN")
                            customTaskText = ""
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                }
            }

            // Tasks List
            Text(
                text = "Današnji Zadaci 🌟",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (task.isCompleted) 2.dp else 1.dp,
                            color = if (task.isCompleted) SoftGold else MaterialTheme.colorScheme.outline
                        )
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
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onToggleTask(task)
                                            if (!task.isCompleted) {
                                                Toast.makeText(context, "Bravo! Dobila si zvezdicu ⭐✨", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    shape = CircleShape,
                                    color = if (task.isCompleted) SoftGold else Color.Transparent,
                                    border = BorderStroke(2.dp, SoftGold)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (task.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Završeno",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Zvezdica",
                                    tint = SoftGold,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 4.dp)
                                )
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
