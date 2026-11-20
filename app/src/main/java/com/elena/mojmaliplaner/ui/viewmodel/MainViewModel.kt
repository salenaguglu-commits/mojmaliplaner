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
import com.elena.mojmaliplaner.data.StarRewardResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class StarType {
    BRONZE,
    SILVER,
    GOLD
}

data class StarEvent(
    val type: StarType,
    val messageSr: String,
    val messageEn: String
)

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

    val appLanguage: StateFlow<String> = userPreferencesRepository.appLanguageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SYSTEM"
    )

    val bronzeStars: StateFlow<Int> = userPreferencesRepository.bronzeStarsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val silverStars: StateFlow<Int> = userPreferencesRepository.silverStarsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val availableStars: StateFlow<Int> = userPreferencesRepository.availableStarsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalStars: StateFlow<Int> = userPreferencesRepository.totalStarsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val rewardedTaskIds: StateFlow<Set<String>> = userPreferencesRepository.rewardedTaskIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val solvedQuestionIds: StateFlow<Set<String>> = userPreferencesRepository.solvedQuestionIdsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    private val _starEvents = MutableSharedFlow<StarEvent>()
    val starEvents: SharedFlow<StarEvent> = _starEvents.asSharedFlow()

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

    fun updateAppLanguage(lang: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLanguage(lang)
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

    private val _showGoldStarCelebration = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val showGoldStarCelebration = _showGoldStarCelebration.asSharedFlow()

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val isNowCompleted = !task.isCompleted
            appRepository.updateTask(task.copy(isCompleted = isNowCompleted))

            if (isNowCompleted && (task.category == "DNEVNI_PLAN" || task.category == "ŠKOLA")) {
                val taskIdStr = task.id.toString()
                val currentRewarded = userPreferencesRepository.getRewardedTaskIdsSynchronously()

                if (!currentRewarded.contains(taskIdStr)) {
                    val result = userPreferencesRepository.addSilverStar(taskIdStr)
                    when (result) {
                        StarRewardResult.SILVER_ADDED -> {
                            _starEvents.emit(StarEvent(StarType.SILVER, "Zadatak završen! +1 Srebrna Zvezdica 🥈", "Task done! +1 Silver Star 🥈"))
                        }
                        StarRewardResult.GOLD_CONVERTED -> {
                            _starEvents.emit(StarEvent(StarType.GOLD, "5 srebrnih = +1 Zlatna Zvezdica! ⭐🎉", "5 Silver = +1 Gold Star! ⭐🎉"))
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            appRepository.updateTask(task)
        }
    }

    private val freeTimeSeedMutex = Mutex()

    fun seedFreeTimeDefaultsIfNeeded(isSerbia: Boolean) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            freeTimeSeedMutex.withLock {
                if (userPreferencesRepository.isFreeTimeDefaultsSeeded()) return@withLock
                val existingTasks = appRepository.allTasks.first()

                if (existingTasks.none { it.category == "SLOBODNO_IDEJA" }) {
                    val defaults = if (isSerbia) {
                        listOf(
                            "Nacrtaj novu avanturu Mije i Blekija 🎨",
                            "Napravi slagalicu ili lego kulu 🧱",
                            "Pravi figurice od plastelina 🌈",
                            "Nauči novu društvenu igru sa drugarima 🎲"
                        )
                    } else {
                        listOf(
                            "Draw a new adventure of Mia and Blacky 🎨",
                            "Build a puzzle or Lego tower 🧱",
                            "Make clay figurines 🌈",
                            "Learn a new board game with friends 🎲"
                        )
                    }
                    defaults.forEach { title ->
                        appRepository.insertTask(
                            TaskEntity(
                                title = title,
                                category = "SLOBODNO_IDEJA",
                                dateMillis = System.currentTimeMillis(),
                                colorHex = "#6EC5E9",
                                isCompleted = false,
                                subjectName = null
                            )
                        )
                    }
                }

                if (existingTasks.none { it.category == "SLOBODNO_MEDA" }) {
                    val defaults = if (isSerbia) {
                        listOf(
                            "Mali Princ" to "5",
                            "Kralj Lavova" to "5",
                            "Gospodar prstenova" to "5",
                            "Hobit" to "5"
                        )
                    } else {
                        listOf(
                            "The Little Prince" to "5",
                            "The Lion King" to "5",
                            "The Lord of the Rings" to "5",
                            "The Hobbit" to "5"
                        )
                    }
                    defaults.forEach { (title, rating) ->
                        appRepository.insertTask(
                            TaskEntity(
                                title = title,
                                category = "SLOBODNO_MEDA",
                                dateMillis = System.currentTimeMillis(),
                                colorHex = "#C49ED4",
                                isCompleted = false,
                                subjectName = rating
                            )
                        )
                    }
                }

                if (existingTasks.none { it.category == "SLOBODNO_ZELJA" }) {
                    val defaults = if (isSerbia) {
                        listOf(
                            "Odlazak u prirodu sa Blekijem 🐕",
                            "Pravljenje neke sjajne poslastice 🧁",
                            "Vožnja bicikla 🚲",
                            "Komponovanje nove kompozicije 🎼"
                        )
                    } else {
                        listOf(
                            "Trip to nature with Blacky 🐕",
                            "Baking a delicious treat 🧁",
                            "Riding a bicycle 🚲",
                            "Composing a new tune 🎼"
                        )
                    }
                    defaults.forEach { title ->
                        appRepository.insertTask(
                            TaskEntity(
                                title = title,
                                category = "SLOBODNO_ZELJA",
                                dateMillis = System.currentTimeMillis(),
                                colorHex = "#CBA84E",
                                isCompleted = false,
                                subjectName = null
                            )
                        )
                    }
                }

                userPreferencesRepository.setFreeTimeDefaultsSeeded(true)
            }
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
            val result = userPreferencesRepository.addBronzeStar()
            when (result) {
                StarRewardResult.BRONZE_ADDED -> {
                    _starEvents.emit(StarEvent(StarType.BRONZE, "Nega ljubimca! +1 Bronzana Zvezdica 🥉", "Pet Care! +1 Bronze Star 🥉"))
                }
                StarRewardResult.SILVER_CONVERTED -> {
                    _starEvents.emit(StarEvent(StarType.SILVER, "5 bronzanih = +1 Srebrna Zvezdica! 🥈✨", "5 Bronze = +1 Silver Star! 🥈✨"))
                }
                StarRewardResult.GOLD_CONVERTED -> {
                    _starEvents.emit(StarEvent(StarType.GOLD, "5 srebrnih = +1 Zlatna Zvezdica! ⭐🎉", "5 Silver = +1 Gold Star! ⭐🎉"))
                }
                else -> {}
            }
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

                userPreferencesRepository.restoreBackupPreferences(
                    userName = parsed.userName,
                    catName = parsed.catName,
                    dogName = parsed.dogName,
                    userPin = parsed.userPin,
                    bronzeStars = parsed.bronzeStars,
                    silverStars = parsed.silverStars,
                    availableStars = parsed.availableStars,
                    totalStars = parsed.totalStars,
                    solvedQuestionIds = parsed.solvedQuestionIds,
                    rewardedTaskIds = parsed.rewardedTaskIds
                )

                parsed.tasks.forEach { task ->
                    appRepository.insertTask(task)
                }

                if (parsed.tasks.any { it.category.startsWith("SLOBODNO_") }) {
                    userPreferencesRepository.setFreeTimeDefaultsSeeded(true)
                }

                parsed.petLogs.forEach { log ->
                    appRepository.insertPetLog(log)
                }

                parsed.diaryEntries.forEach { entry ->
                    appRepository.insertDiaryEntry(entry)
                }

                parsed.musicSheets.forEach { sheet ->
                    appRepository.insertMusicSheet(sheet)
                }

                parsed.unlockedStickers.forEach { sticker ->
                    appRepository.insertSticker(sticker)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Stickers
    val unlockedStickers = appRepository.allUnlockedStickers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun openStickerPack(onOpened: (Int) -> Unit, onNotEnoughStars: () -> Unit) {
        viewModelScope.launch {
            val success = userPreferencesRepository.spendStars(3)
            if (success) {
                val currentUnlocked = unlockedStickers.value.map { it.stickerId }
                val availableIds = (1..20).toList()
                val lockedIds = availableIds - currentUnlocked

                // Pick a random sticker, prioritize locked ones
                val newStickerId = if (lockedIds.isNotEmpty()) {
                    lockedIds.random()
                } else {
                    availableIds.random() // If all unlocked, just give a duplicate
                }

                if (!currentUnlocked.contains(newStickerId)) {
                    appRepository.insertSticker(
                        com.elena.mojmaliplaner.data.db.UnlockedStickerEntity(
                            stickerId = newStickerId,
                            unlockedAtMillis = System.currentTimeMillis(),
                            isNew = true
                        )
                    )
                }
                
                onOpened(newStickerId)
            } else {
                onNotEnoughStars()
            }
        }
    }

    fun markStickerAsSeen(stickerId: Int) {
        viewModelScope.launch {
            val sticker = unlockedStickers.value.find { it.stickerId == stickerId }
            if (sticker != null && sticker.isNew) {
                appRepository.updateSticker(sticker.copy(isNew = false))
            }
        }
    }

    fun markQuestionSolved(questionId: String) {
        viewModelScope.launch {
            userPreferencesRepository.markQuestionSolved(questionId)
        }
    }

    // Language Game State
    private val _languageStreak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val languageStreak: StateFlow<Int> = _languageStreak.asStateFlow()

    fun submitLanguageAnswer(isCorrect: Boolean) {
        viewModelScope.launch {
            if (isCorrect) {
                val newStreak = _languageStreak.value + 1
                if (newStreak >= 5) {
                    userPreferencesRepository.addGoldStar()
                    _showGoldStarCelebration.emit(Unit)
                    _languageStreak.value = 0
                } else {
                    _languageStreak.value = newStreak
                }
            } else {
                _languageStreak.value = 0
            }
        }
    }
}
