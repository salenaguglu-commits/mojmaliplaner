package com.elena.mojmaliplaner.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.ui.navigation.Routes
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.BrightCreamText
import com.elena.mojmaliplaner.ui.theme.DarkCardBase
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import com.elena.mojmaliplaner.ui.theme.SoftGray
import com.elena.mojmaliplaner.ui.theme.SoftSilverGray

data class PlannerCategory(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val lightBgColor: Color,
    val borderColor: Color,
    val emoji: String,
    val watermarkMotive: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    catName: String,
    dogName: String,
    appLanguage: String,
    todayTasks: List<TaskEntity>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onUpdateAppLanguage: (String) -> Unit,
    onNavigateToRoute: (route: String) -> Unit,
    onCheckPin: (String) -> Boolean,
    onSaveSettings: (userName: String, catName: String, dogName: String, newPin: String?) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: (Uri) -> Unit,
    onResetAllData: () -> Unit
) {
    var showPinDialogForDiary by remember { mutableStateOf(false) }
    var showPinDialogForSettings by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val shareTitle = stringResource(R.string.share_plan_title, userName)
    val shareHeader = stringResource(R.string.share_tasks_header)
    val shareEmpty = stringResource(R.string.share_empty_tasks)
    val shareFooter = stringResource(R.string.share_footer)
    val shareChooser = stringResource(R.string.share_chooser_title)
    val msgCopied = stringResource(R.string.msg_plan_copied)

    val categories = listOf(
        PlannerCategory(
            route = Routes.CALENDAR,
            title = stringResource(R.string.cat_calendar_title),
            subtitle = stringResource(R.string.cat_calendar_sub),
            icon = Icons.Default.DateRange,
            lightBgColor = PastelBlue,
            borderColor = PastelBlue,
            emoji = "📅",
            watermarkMotive = "🐱⏰✨"
        ),
        PlannerCategory(
            route = Routes.DAILY_PLAN,
            title = stringResource(R.string.cat_daily_plan_title),
            subtitle = stringResource(R.string.cat_daily_plan_sub),
            icon = Icons.Default.Edit,
            lightBgColor = PastelPurple,
            borderColor = PastelPurple,
            emoji = "📝",
            watermarkMotive = "🐈‍⬛📝"
        ),
        PlannerCategory(
            route = Routes.STUDY,
            title = stringResource(R.string.cat_study_title),
            subtitle = stringResource(R.string.cat_study_sub),
            icon = Icons.Default.Star,
            lightBgColor = SoftGold,
            borderColor = SoftGold,
            emoji = "📚",
            watermarkMotive = "🦉👓📚"
        ),
        PlannerCategory(
            route = Routes.FREE_TIME,
            title = stringResource(R.string.cat_free_time_title),
            subtitle = stringResource(R.string.cat_free_time_sub),
            icon = Icons.Default.PlayArrow,
            lightBgColor = SoftGray,
            borderColor = SoftSilverGray,
            emoji = "🎮",
            watermarkMotive = "🐈‍⬛🧶"
        ),
        PlannerCategory(
            route = Routes.PETS,
            title = stringResource(R.string.cat_pets_title),
            subtitle = stringResource(R.string.cat_pets_sub),
            icon = Icons.Default.Favorite,
            lightBgColor = PastelPurple,
            borderColor = PastelPurple,
            emoji = "🐾",
            watermarkMotive = "🐱🐶🐾"
        ),
        PlannerCategory(
            route = Routes.MUSIC,
            title = stringResource(R.string.cat_music_title),
            subtitle = stringResource(R.string.cat_music_sub),
            icon = Icons.Default.Star,
            lightBgColor = PastelBlue,
            borderColor = PastelBlue,
            emoji = "🎵",
            watermarkMotive = "🐈‍⬛🎧🎶"
        ),
        PlannerCategory(
            route = Routes.ALBUM,
            title = stringResource(R.string.cat_album_title),
            subtitle = stringResource(R.string.cat_album_sub),
            icon = Icons.Default.Star,
            lightBgColor = SoftGold,
            borderColor = SoftGold,
            emoji = "🌟",
            watermarkMotive = "✨🐾⭐"
        ),
        PlannerCategory(
            route = Routes.LANGUAGE_GAME,
            title = stringResource(R.string.cat_language_title),
            subtitle = stringResource(R.string.cat_language_sub),
            icon = Icons.Default.Edit,
            lightBgColor = PastelPurple,
            borderColor = PastelPurple,
            emoji = "🌍",
            watermarkMotive = "💬📝"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            showPinDialogForSettings = true
                                        }
                                    )
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "🐱",
                                fontSize = 24.sp
                            )
                        }

                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = {
                            val planText = buildString {
                                append("$shareTitle\n\n")
                                if (todayTasks.isNotEmpty()) {
                                    append("$shareHeader\n")
                                    todayTasks.forEach { task ->
                                        val status = if (task.isCompleted) "✅" else "▫️"
                                        append("$status ${task.title}\n")
                                    }
                                } else {
                                    append("$shareEmpty\n")
                                }
                                append("\n$shareFooter")
                            }

                            // 1. Copy to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText(shareTitle, planText)
                            clipboard?.setPrimaryClip(clip)

                            // 2. Share Intent (Viber, WhatsApp, Copy)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                                putExtra(Intent.EXTRA_TEXT, planText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, shareChooser))
                            Toast.makeText(context, msgCopied, Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = BorderStroke(1.dp, if (isDarkTheme) SoftGold else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_send_plan),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isDarkTheme) SoftGold else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = onToggleTheme) {
                        Text(
                            text = if (isDarkTheme) "☀️" else "🌙",
                            fontSize = 20.sp
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isDarkTheme) 0.15f else 0.10f)
            ) {
                Text(
                    text = "🐱",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp)
                )

                Text(
                    text = "🌸",
                    fontSize = 70.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = "🐶 🐈‍⬛",
                    fontSize = 60.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 110.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val numRows = (categories.size + 1) / 2
                    for (rowIndex in 0 until numRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val leftCategory = categories.getOrNull(rowIndex * 2)
                            val rightCategory = categories.getOrNull(rowIndex * 2 + 1)

                            if (leftCategory != null) {
                                PlannerGridCard(
                                    category = leftCategory,
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateToRoute(leftCategory.route) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            if (rightCategory != null) {
                                PlannerGridCard(
                                    category = rightCategory,
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateToRoute(rightCategory.route) }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clickable { showPinDialogForDiary = true },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) DarkCardBase else SoftGold
                    ),
                    border = BorderStroke(2.dp, SoftGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        Text(
                            text = "🐈‍⬛ 🦉 🌙 🌳",
                            fontSize = 28.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-10).dp, y = (4).dp)
                                .alpha(if (isDarkTheme) 0.25f else 0.18f)
                                .graphicsLayer { rotationZ = -4f }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(46.dp),
                                    shape = CircleShape,
                                    color = if (isDarkTheme) SoftGold else BlackCharcoal
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Zaštićeni Dnevnik",
                                            tint = if (isDarkTheme) BlackCharcoal else SoftGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.btn_diary_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) SoftGold else BlackCharcoal,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "📖",
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showPinDialogForDiary) {
        PinDialog(
            onDismissRequest = { showPinDialogForDiary = false },
            onPinVerified = {
                showPinDialogForDiary = false
                onNavigateToRoute(Routes.DIARY)
            },
            onCheckPin = onCheckPin
        )
    }

    if (showPinDialogForSettings) {
        PinDialog(
            onDismissRequest = { showPinDialogForSettings = false },
            onPinVerified = {
                showPinDialogForSettings = false
                showSettingsDialog = true
            },
            onCheckPin = onCheckPin
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentUserName = userName,
            currentCatName = catName,
            currentDogName = dogName,
            currentAppLanguage = appLanguage,
            onSaveSettings = { newUserName, newCatName, newDogName, newPin ->
                onSaveSettings(newUserName, newCatName, newDogName, newPin)
            },
            onUpdateAppLanguage = onUpdateAppLanguage,
            onExportBackup = onExportBackup,
            onImportBackup = onImportBackup,
            onResetAllData = onResetAllData,
            onDismissRequest = { showSettingsDialog = false }
        )
    }
}

@Composable
fun PlannerGridCard(
    category: PlannerCategory,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBgColor = if (isDarkTheme) {
        DarkCardBase
    } else {
        category.lightBgColor
    }

    val textColor = if (isDarkTheme) BrightCreamText else BlackCharcoal

    val cardBorder = if (isDarkTheme) {
        BorderStroke(1.5.dp, category.borderColor)
    } else {
        BorderStroke(1.dp, category.lightBgColor)
    }

    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
        ) {
            if (isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = category.lightBgColor.copy(alpha = 0.12f)
                ) {}
            }

            Text(
                text = category.watermarkMotive,
                fontSize = 40.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .alpha(if (isDarkTheme) 0.22f else 0.18f)
                    .graphicsLayer { rotationZ = -10f }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = if (isDarkTheme) category.lightBgColor.copy(alpha = 0.25f) else BlackCharcoal.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                tint = if (isDarkTheme) category.borderColor else BlackCharcoal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = category.emoji,
                        fontSize = 20.sp
                    )
                }

                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
