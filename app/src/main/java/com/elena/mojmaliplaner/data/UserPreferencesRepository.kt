package com.elena.mojmaliplaner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class StarRewardResult {
    BRONZE_ADDED,
    SILVER_ADDED,
    SILVER_CONVERTED,
    GOLD_CONVERTED
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PIN = stringPreferencesKey("user_pin")
        val CAT_NAME = stringPreferencesKey("cat_name")
        val DOG_NAME = stringPreferencesKey("dog_name")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val BRONZE_STARS = intPreferencesKey("bronze_stars")
        val SILVER_STARS = intPreferencesKey("silver_stars")
        val AVAILABLE_STARS = intPreferencesKey("available_stars")
        val TOTAL_STARS = intPreferencesKey("total_stars")
        val REWARDED_TASK_IDS = stringSetPreferencesKey("rewarded_task_ids")
        val SOLVED_QUESTION_IDS = stringSetPreferencesKey("solved_question_ids")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_NAME] ?: "Elena"
    }

    val userPinFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_PIN] ?: "1111"
    }

    val catNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CAT_NAME] ?: "Mia"
    }

    val dogNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DOG_NAME] ?: "Bleki"
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
    }

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    val appLanguageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.APP_LANGUAGE] ?: "SYSTEM"
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_LANGUAGE] = lang
        }
    }

    val bronzeStarsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BRONZE_STARS] ?: 0
    }

    val silverStarsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SILVER_STARS] ?: 0
    }

    val availableStarsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AVAILABLE_STARS] ?: 0
    }

    val totalStarsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.TOTAL_STARS] ?: 0
    }

    val rewardedTaskIdsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.REWARDED_TASK_IDS] ?: emptySet()
    }

    val solvedQuestionIdsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SOLVED_QUESTION_IDS] ?: emptySet()
    }

    suspend fun markQuestionSolved(questionId: String) {
        context.dataStore.edit { prefs ->
            val currentSet = prefs[PreferencesKeys.SOLVED_QUESTION_IDS] ?: emptySet()
            prefs[PreferencesKeys.SOLVED_QUESTION_IDS] = currentSet + questionId
        }
    }

    suspend fun getRewardedTaskIdsSynchronously(): Set<String> {
        val prefs = context.dataStore.data.first()
        return prefs[PreferencesKeys.REWARDED_TASK_IDS] ?: emptySet()
    }

    /**
     * Adds 1 Bronze Star (e.g. from Pet Care).
     * Converts 5 Bronze -> 1 Silver, and 5 Silver -> 1 Gold automatically!
     */
    suspend fun addBronzeStar(): StarRewardResult {
        var result = StarRewardResult.BRONZE_ADDED
        context.dataStore.edit { prefs ->
            val currentBronze = prefs[PreferencesKeys.BRONZE_STARS] ?: 0
            if (currentBronze + 1 >= 5) {
                prefs[PreferencesKeys.BRONZE_STARS] = 0
                val currentSilver = prefs[PreferencesKeys.SILVER_STARS] ?: 0
                if (currentSilver + 1 >= 5) {
                    prefs[PreferencesKeys.SILVER_STARS] = 0
                    val currentAvailable = prefs[PreferencesKeys.AVAILABLE_STARS] ?: 0
                    val currentTotal = prefs[PreferencesKeys.TOTAL_STARS] ?: 0
                    prefs[PreferencesKeys.AVAILABLE_STARS] = currentAvailable + 1
                    prefs[PreferencesKeys.TOTAL_STARS] = currentTotal + 1
                    result = StarRewardResult.GOLD_CONVERTED
                } else {
                    prefs[PreferencesKeys.SILVER_STARS] = currentSilver + 1
                    result = StarRewardResult.SILVER_CONVERTED
                }
            } else {
                prefs[PreferencesKeys.BRONZE_STARS] = currentBronze + 1
                result = StarRewardResult.BRONZE_ADDED
            }
        }
        return result
    }

    /**
     * Adds 1 Silver Star (e.g. from unique task completion).
     * Converts 5 Silver -> 1 Gold automatically!
     */
    suspend fun addSilverStar(taskIdToMark: String? = null): StarRewardResult {
        var result = StarRewardResult.SILVER_ADDED
        context.dataStore.edit { prefs ->
            if (taskIdToMark != null) {
                val currentSet = prefs[PreferencesKeys.REWARDED_TASK_IDS] ?: emptySet()
                prefs[PreferencesKeys.REWARDED_TASK_IDS] = currentSet + taskIdToMark
            }

            val currentSilver = prefs[PreferencesKeys.SILVER_STARS] ?: 0
            if (currentSilver + 1 >= 5) {
                prefs[PreferencesKeys.SILVER_STARS] = 0
                val currentAvailable = prefs[PreferencesKeys.AVAILABLE_STARS] ?: 0
                val currentTotal = prefs[PreferencesKeys.TOTAL_STARS] ?: 0
                prefs[PreferencesKeys.AVAILABLE_STARS] = currentAvailable + 1
                prefs[PreferencesKeys.TOTAL_STARS] = currentTotal + 1
                result = StarRewardResult.GOLD_CONVERTED
            } else {
                prefs[PreferencesKeys.SILVER_STARS] = currentSilver + 1
                result = StarRewardResult.SILVER_ADDED
            }
        }
        return result
    }

    /**
     * Directly adds 1 Gold Star (e.g. from Language Game 5-streak).
     */
    suspend fun addGoldStar() {
        context.dataStore.edit { prefs ->
            val currentAvailable = prefs[PreferencesKeys.AVAILABLE_STARS] ?: 0
            val currentTotal = prefs[PreferencesKeys.TOTAL_STARS] ?: 0
            prefs[PreferencesKeys.AVAILABLE_STARS] = currentAvailable + 1
            prefs[PreferencesKeys.TOTAL_STARS] = currentTotal + 1
        }
    }

    suspend fun spendStars(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentAvailable = prefs[PreferencesKeys.AVAILABLE_STARS] ?: 0
            if (currentAvailable >= amount) {
                prefs[PreferencesKeys.AVAILABLE_STARS] = currentAvailable - amount
                success = true
            }
        }
        return success
    }

    suspend fun saveOnboardingData(name: String, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = name.ifBlank { "Elena" }
            prefs[PreferencesKeys.USER_PIN] = if (pin.length == 4) pin else "1111"
            prefs[PreferencesKeys.IS_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun updateUserPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_PIN] = pin
        }
    }

    suspend fun updatePetNames(catName: String, dogName: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CAT_NAME] = catName
            prefs[PreferencesKeys.DOG_NAME] = dogName
        }
    }

    suspend fun resetAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
