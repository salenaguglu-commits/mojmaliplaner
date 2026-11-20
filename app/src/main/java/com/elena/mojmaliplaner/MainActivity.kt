package com.elena.mojmaliplaner

import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

            val overrideConfig = remember(currentConfig, targetLocale) {
                Configuration(currentConfig).apply {
                    setLocale(targetLocale)
                }
            }

            val localizedContext = remember(overrideConfig) {
                val configResources = createConfigurationContext(overrideConfig).resources
                object : ContextWrapper(this@MainActivity) {
                    override fun getResources(): Resources = configResources
                }
            }

            CompositionLocalProvider(
                LocalConfiguration provides overrideConfig,
                LocalContext provides localizedContext
            ) {
                MojMaliPlanerTheme(darkTheme = isDarkTheme) {
                    AppNavigation(viewModel = mainViewModel)
                }
            }
        }
    }
}
