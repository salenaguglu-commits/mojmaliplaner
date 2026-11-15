package com.elena.mojmaliplaner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_PIN = stringPreferencesKey("user_pin")
        val CAT_NAME = stringPreferencesKey("cat_name")
        val DOG_NAME = stringPreferencesKey("dog_name")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
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
