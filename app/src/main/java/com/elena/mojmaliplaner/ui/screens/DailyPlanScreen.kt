package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.ui.components.BronzeColor
import com.elena.mojmaliplaner.ui.components.ConfettiBurstOverlay
import com.elena.mojmaliplaner.ui.components.SilverColor
import com.elena.mojmaliplaner.ui.components.bouncyClickable
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.SoftGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DailyPlanScreen(
    tasks: List<TaskEntity>,
    bronzeStars: Int,
    silverStars: Int,
    availableStars: Int,
    onAddTask: (title: String, category: String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var customTaskText by remember { mutableStateOf("") }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val isSerbia = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    val presetTemplates = if (isSerbia) {
        listOf("Sredi sobu 🧹", "Uradi domaći 📚", "Spakuj ranac 🎒", "Popij vodu 💧")
    } else {
        listOf("Clean room 🧹", "Do homework 📚", "Pack backpack 🎒", "Drink water 💧")
    }

    val bronzeProgress by animateFloatAsState(
        targetValue = (bronzeStars / 5f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bronze_progress"
    )
    val silverProgress by animateFloatAsState(
        targetValue = (silverStars / 5f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "silver_progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.daily_plan_topbar),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Preset Quick Templates
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isSerbia) "Brzi unos zadataka za danas ✨" else "Quick task templates ✨",
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
                                    modifier = Modifier.bouncyClickable(pressedScale = 0.93f) {
                                        onAddTask(template, "DNEVNI_PLAN")
                                        Toast.makeText(context, "${if (isSerbia) "Dodato" else "Added"}: $template ⭐", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    shadowElevation = 1.dp
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
                        placeholder = { Text(if (isSerbia) "Dodaj svoj plan za danas..." else "Add your plan for today...") },
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

                // Animated Star Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        SoftGold.copy(alpha = 0.16f)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🥉 $bronzeStars/5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    LinearProgressIndicator(
                                        progress = { bronzeProgress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = BronzeColor,
                                        trackColor = BronzeColor.copy(alpha = 0.2f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🥈 $silverStars/5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    LinearProgressIndicator(
                                        progress = { silverProgress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = SilverColor,
                                        trackColor = SilverColor.copy(alpha = 0.25f),
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SoftGold.copy(alpha = 0.25f),
                                border = BorderStroke(1.5.dp, SoftGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⭐", fontSize = 18.sp)
                                    AnimatedContent(
                                        targetState = availableStars,
                                        transitionSpec = {
                                            (slideInVertically { -it } + fadeIn())
                                                .togetherWith(slideOutVertically { it } + fadeOut())
                                        },
                                        label = "star_counter"
                                    ) { stars ->
                                        Text(
                                            text = "$stars",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = SoftGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tasks List
                Text(
                    text = if (isSerbia) "Današnji Zadaci 🌟" else "Today's Tasks 🌟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val cardBorderColor by animateColorAsState(
                            targetValue = if (task.isCompleted) SoftGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                            animationSpec = tween(280),
                            label = "task_border"
                        )
                        val checkBgColor by animateColorAsState(
                            targetValue = if (task.isCompleted) SoftGold else Color.Transparent,
                            animationSpec = tween(250),
                            label = "check_bg"
                        )
                        val checkScale by animateFloatAsState(
                            targetValue = if (task.isCompleted) 1.08f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "check_scale"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (task.isCompleted) 2.dp else 1.dp,
                                color = cardBorderColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 3.dp else 1.dp)
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
                                            .size(34.dp)
                                            .scale(checkScale)
                                            .clip(CircleShape)
                                            .bouncyClickable(pressedScale = 0.86f) {
                                                val wasIncomplete = !task.isCompleted
                                                onToggleTask(task)
                                                if (wasIncomplete) {
                                                    confettiTrigger++
                                                    Toast.makeText(
                                                        context,
                                                        if (isSerbia) "Bravo! Dobila si zvezdicu ⭐✨" else "Great job! You earned a star ⭐✨",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                        shape = CircleShape,
                                        color = checkBgColor,
                                        border = BorderStroke(2.dp, SoftGold)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = task.isCompleted,
                                                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                                exit = scaleOut() + fadeOut()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Završeno",
                                                    tint = BlackCharcoal,
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

                                AnimatedVisibility(
                                    visible = task.isCompleted,
                                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                    exit = scaleOut() + fadeOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Zvezdica",
                                        tint = SoftGold,
                                        modifier = Modifier
                                            .size(26.dp)
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

            if (confettiTrigger > 0) {
                ConfettiBurstOverlay(
                    triggerKey = confettiTrigger,
                    particleCount = 52,
                    durationSeconds = 2.1f,
                    originYFraction = 0.35f
                )
            }
        }
    }
}
