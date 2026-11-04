package com.expensetracker.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.expensetracker.domain.model.ThemeMode
import com.expensetracker.presentation.ui.navigation.AppNavHost
import com.expensetracker.presentation.ui.navigation.Screen
import com.expensetracker.presentation.ui.theme.ExpenseTrackerTheme
import com.expensetracker.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe theme changes
        lifecycleScope.launch {
            settingsViewModel.settings.collect { settings ->
                // Theme is applied in ExpenseTrackerTheme
            }
        }

        setContent {
            ExpenseTrackerTheme(
                themeMode = settingsViewModel.settings.value?.theme ?: ThemeMode.SYSTEM
            ) {
                AppNavHost()
            }
        }
    }
}