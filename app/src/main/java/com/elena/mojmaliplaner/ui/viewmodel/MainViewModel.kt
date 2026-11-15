package com.elena.mojmaliplaner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elena.mojmaliplaner.data.AppRepository
import com.elena.mojmaliplaner.data.UserPreferencesRepository
import com.elena.mojmaliplaner.data.db.CustomTrackEntity
import com.elena.mojmaliplaner.data.db.DiaryEntryEntity
import com.elena.mojmaliplaner.data.db.MusicSheetEntity
import com.elena.mojmaliplaner.data.db.PetLogEntity
import com.elena.mojmaliplaner.data.db.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val appRepository = AppRepository(application)

    // User Preferences
    val userName: StateFlow<String> = userPreferencesRepository.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Elena"
    )

    val userPin: StateFlow<String> = userPreferencesRepository.userPinFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "1111"
    )

    val catName: StateFlow<String> = userPreferencesRepository.catNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Mia"
    )

    val dogName: StateFlow<String> = userPreferencesRepository.dogNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Bleki"
    )

    val isOnboardingCompleted: StateFlow<Boolean?> = userPreferencesRepository.isOnboardingCompletedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val isDarkTheme: StateFlow<Boolean> = userPreferencesRepository.isDarkThemeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Database Streams
    val allTasks: StateFlow<List<TaskEntity>> = appRepository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPetLogs: StateFlow<List<PetLogEntity>> = appRepository.allPetLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDiaryEntries: StateFlow<List<DiaryEntryEntity>> = appRepository.allDiaryEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMusicSheets: StateFlow<List<MusicSheetEntity>> = appRepository.allMusicSheets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomTracks: StateFlow<List<CustomTrackEntity>> = appRepository.allCustomTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Preferences Actions
    fun saveOnboarding(name: String, pin: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveOnboardingData(name, pin)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            userPreferencesRepository.setDarkTheme(!isDarkTheme.value)
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        return enteredPin == userPin.value
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserName(name)
        }
    }

    fun updatePetNames(catName: String, dogName: String) {
        viewModelScope.launch {
            userPreferencesRepository.updatePetNames(catName, dogName)
        }
    }

    fun updateUserPin(newPin: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserPin(newPin)
        }
    }

    fun resetAllData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            appRepository.clearAllData()
            userPreferencesRepository.resetAllData()
            
            try {
                val audioDir = java.io.File(getApplication<Application>().filesDir, "custom_audio")
                if (audioDir.exists()) {
                    audioDir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Task Actions
    fun addTask(
        title: String,
        category: String,
        dateMillis: Long = System.currentTimeMillis(),
        colorHex: String = "#6EC5E9",
        subjectName: String? = null
    ) {
        val safeColor = try {
            android.graphics.Color.parseColor(colorHex)
            colorHex
        } catch (e: Exception) {
            "#6EC5E9"
        }

        viewModelScope.launch {
            appRepository.insertTask(
                TaskEntity(
                    title = title,
                    category = category,
                    dateMillis = dateMillis,
                    colorHex = safeColor,
                    isCompleted = false,
                    subjectName = subjectName
                )
            )
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            appRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            appRepository.deleteTask(task)
        }
    }

    // Pet Actions
    fun logPetAction(petType: String, actionType: String) {
        viewModelScope.launch {
            appRepository.logPetAction(petType, actionType)
        }
    }

    // Diary Actions
    fun addDiaryEntry(title: String, content: String, moodEmoji: String = "🐱") {
        viewModelScope.launch {
            appRepository.insertDiaryEntry(
                DiaryEntryEntity(
                    dateMillis = System.currentTimeMillis(),
                    title = title,
                    content = content,
                    moodEmoji = moodEmoji
                )
            )
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            appRepository.deleteDiaryEntry(entry)
        }
    }

    // Music Actions
    fun addMusicSheet(title: String, category: String, fileUri: String = "") {
        viewModelScope.launch {
            appRepository.insertMusicSheet(
                MusicSheetEntity(
                    title = title,
                    instrumentOrCategory = category,
                    fileUri = fileUri
                )
            )
        }
    }

    fun deleteMusicSheet(sheet: MusicSheetEntity) {
        viewModelScope.launch {
            appRepository.deleteMusicSheet(sheet)
        }
    }

    fun addCustomTrack(title: String, filePath: String) {
        viewModelScope.launch {
            appRepository.insertCustomTrack(
                CustomTrackEntity(
                    title = title,
                    filePath = filePath
                )
            )
        }
    }

    fun deleteCustomTrack(track: CustomTrackEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            appRepository.deleteCustomTrack(track)
            try {
                val file = java.io.File(track.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreBackupData(
        context: android.content.Context,
        jsonUri: android.net.Uri
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(jsonUri)?.use { input ->
                    input.bufferedReader().readText()
                } ?: return@launch

                val parsed = com.elena.mojmaliplaner.data.BackupManager.parseBackupJson(jsonString) ?: return@launch

                if (!parsed.userName.isNullOrBlank()) updateUserName(parsed.userName)
                if (!parsed.catName.isNullOrBlank() && !parsed.dogName.isNullOrBlank()) {
                    updatePetNames(parsed.catName, parsed.dogName)
                }
                if (!parsed.userPin.isNullOrBlank()) updateUserPin(parsed.userPin)

                parsed.tasks.forEach { task ->
                    appRepository.insertTask(task)
                }

                parsed.petLogs.forEach { log ->
                    appRepository.logPetAction(log.petType, log.actionType)
                }

                parsed.diaryEntries.forEach { entry ->
                    appRepository.insertDiaryEntry(entry)
                }

                parsed.musicSheets.forEach { sheet ->
                    appRepository.insertMusicSheet(sheet)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
