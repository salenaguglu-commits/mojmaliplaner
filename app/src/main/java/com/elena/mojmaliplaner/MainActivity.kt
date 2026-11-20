package com.elena.mojmaliplaner

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elena.mojmaliplaner.ui.navigation.AppNavigation
import com.elena.mojmaliplaner.ui.theme.MojMaliPlanerTheme
import com.elena.mojmaliplaner.ui.viewmodel.MainViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val appLanguage by mainViewModel.appLanguage.collectAsState()

            val currentConfig = LocalConfiguration.current

            val targetLocale = when (appLanguage) {
                "SR" -> Locale("sr", "RS")
                "EN" -> Locale.ENGLISH
                else -> Locale.getDefault()
            }

            val overrideConfig = Configuration(currentConfig).apply {
                setLocale(targetLocale)
            }

            CompositionLocalProvider(
                LocalConfiguration provides overrideConfig
            ) {
                MojMaliPlanerTheme(darkTheme = isDarkTheme) {
                    AppNavigation(viewModel = mainViewModel)
                }
            }
        }
    }
}
