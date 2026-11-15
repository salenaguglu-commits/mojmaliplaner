package com.elena.mojmaliplaner

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elena.mojmaliplaner.data.CountryDetector
import com.elena.mojmaliplaner.ui.navigation.AppNavigation
import com.elena.mojmaliplaner.ui.theme.MojMaliPlanerTheme
import com.elena.mojmaliplaner.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val localizedContext = CountryDetector.applyAppLocale(newBase)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            MojMaliPlanerTheme(darkTheme = isDarkTheme) {
                AppNavigation(viewModel = mainViewModel)
            }
        }
    }
}