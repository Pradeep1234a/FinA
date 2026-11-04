package com.expensetracker.presentation.ui.screen.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.expensetracker.presentation.ui.MainActivity
import com.expensetracker.presentation.ui.screen.onboarding.OnboardingActivity
import com.expensetracker.presentation.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                SplashScreen(onNavigate = { destination ->
                    when (destination) {
                        "onboarding" -> {
                            startActivity(Intent(this, OnboardingActivity::class.java))
                            finish()
                        }
                        "main" -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                })
            }
        }
    }
}