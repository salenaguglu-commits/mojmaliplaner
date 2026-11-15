package com.elena.mojmaliplaner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elena.mojmaliplaner.ui.screens.CalendarScreen
import com.elena.mojmaliplaner.ui.screens.DailyPlanScreen
import com.elena.mojmaliplaner.ui.screens.DiaryScreen
import com.elena.mojmaliplaner.ui.screens.FreeTimeScreen
import com.elena.mojmaliplaner.ui.screens.HomeScreen
import com.elena.mojmaliplaner.ui.screens.MusicScreen
import com.elena.mojmaliplaner.ui.screens.OnboardingScreen
import com.elena.mojmaliplaner.ui.screens.PetsScreen
import com.elena.mojmaliplaner.ui.screens.StudyScreen
import com.elena.mojmaliplaner.ui.viewmodel.MainViewModel
import com.elena.mojmaliplaner.ui.viewmodel.MusicViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val DAILY_PLAN = "daily_plan"
    const val STUDY = "study"
    const val FREE_TIME = "free_time"
    const val PETS = "pets"
    const val MUSIC = "music"
    const val DIARY = "diary"
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val catName by viewModel.catName.collectAsState()
    val dogName by viewModel.dogName.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val tasks by viewModel.allTasks.collectAsState()
    val petLogs by viewModel.allPetLogs.collectAsState()
    val diaryEntries by viewModel.allDiaryEntries.collectAsState()
    val musicSheets by viewModel.allMusicSheets.collectAsState()
    val customTracks by viewModel.allCustomTracks.collectAsState()

    if (isOnboardingCompleted == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🐈‍⬛ ✨ 🦉", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        val startDestination = if (isOnboardingCompleted == true) Routes.HOME else Routes.ONBOARDING

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onCompleteOnboarding = { name, pin ->
                        viewModel.saveOnboarding(name, pin)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val userPin by viewModel.userPin.collectAsState()

                HomeScreen(
                    userName = userName,
                    catName = catName,
                    dogName = dogName,
                    todayTasks = tasks,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onNavigateToRoute = { route ->
                        navController.navigate(route)
                    },
                    onCheckPin = { enteredPin ->
                        viewModel.verifyPin(enteredPin)
                    },
                    onSaveSettings = { newUserName, newCatName, newDogName, newPin ->
                        viewModel.updateUserName(newUserName)
                        viewModel.updatePetNames(newCatName, newDogName)
                        if (newPin != null) {
                            viewModel.updateUserPin(newPin)
                        }
                    },
                    onExportBackup = {
                        val json = com.elena.mojmaliplaner.data.BackupManager.createBackupJson(
                            userName = userName,
                            catName = catName,
                            dogName = dogName,
                            userPin = userPin,
                            tasks = tasks,
                            petLogs = petLogs,
                            diaryEntries = diaryEntries,
                            musicSheets = musicSheets
                        )
                        com.elena.mojmaliplaner.data.BackupManager.exportAndShareBackup(context, json)
                    },
                    onImportBackup = { jsonUri ->
                        viewModel.restoreBackupData(context, jsonUri)
                    },
                    onResetAllData = {
                        viewModel.resetAllData()
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CALENDAR) {
                CalendarScreen(
                    tasks = tasks,
                    onAddTask = { title, category, colorHex, dateMillis ->
                        viewModel.addTask(title = title, category = category, colorHex = colorHex, dateMillis = dateMillis)
                    },
                    onToggleTask = { task -> viewModel.toggleTaskCompleted(task) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.DAILY_PLAN) {
                DailyPlanScreen(
                    tasks = tasks.filter { it.category == "DNEVNI_PLAN" },
                    onAddTask = { title, category ->
                        viewModel.addTask(title = title, category = category)
                    },
                    onToggleTask = { task -> viewModel.toggleTaskCompleted(task) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.STUDY) {
                StudyScreen(
                    tasks = tasks,
                    onAddStudyTask = { title, subjectName ->
                        viewModel.addTask(title = title, category = "ŠKOLA", subjectName = subjectName)
                    },
                    onToggleTask = { task -> viewModel.toggleTaskCompleted(task) },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FREE_TIME) {
                FreeTimeScreen(
                    tasks = tasks,
                    onAddTask = { title, category, colorHex, subjectName ->
                        viewModel.addTask(title = title, category = category, colorHex = colorHex, subjectName = subjectName)
                    },
                    onDeleteTask = { task -> viewModel.deleteTask(task) },
                    onUpdateTask = { task -> viewModel.toggleTaskCompleted(task) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PETS) {
                PetsScreen(
                    catName = catName,
                    dogName = dogName,
                    petLogs = petLogs,
                    onLogPetAction = { petType, actionType ->
                        viewModel.logPetAction(petType, actionType)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MUSIC) {
                val musicViewModel: MusicViewModel = viewModel()
                MusicScreen(
                    musicViewModel = musicViewModel,
                    musicSheets = musicSheets,
                    customTracks = customTracks,
                    onAddCustomTrackUri = { uri ->
                        musicViewModel.importAudioFile(uri) { title, filePath ->
                            viewModel.addCustomTrack(title, filePath)
                        }
                    },
                    onDeleteCustomTrack = { entity ->
                        viewModel.deleteCustomTrack(entity)
                    },
                    onAddMusicSheet = { title, category, fileUri ->
                        viewModel.addMusicSheet(title, category, fileUri)
                    },
                    onDeleteMusicSheet = { sheet ->
                        viewModel.deleteMusicSheet(sheet)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.DIARY) {
                DiaryScreen(
                    userName = userName,
                    diaryEntries = diaryEntries,
                    onAddDiaryEntry = { title, content, moodEmoji ->
                        viewModel.addDiaryEntry(title, content, moodEmoji)
                    },
                    onDeleteDiaryEntry = { entry ->
                        viewModel.deleteDiaryEntry(entry)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
