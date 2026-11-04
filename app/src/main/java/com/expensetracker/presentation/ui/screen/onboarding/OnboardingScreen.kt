package com.expensetracker.presentation.ui.screen.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.ThemeMode
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val totalSteps by viewModel.totalSteps.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val defaultCategories by viewModel.defaultCategories.collectAsState()
    val selectedDefaultCategories by viewModel.selectedDefaultCategories.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    val isLastStep = currentStep == totalSteps - 1

    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .width((currentStep + 1) * 100 / totalSteps)
                        .fillMaxHeight()
                        .background(Green600)
                )
            }

            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                viewModel.steps.forEachIndexed { index, step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (index <= currentStep) Green600 else Color(0xFFBDBDBD))
                            Text(text = step, fontSize = 10.sp, color = if (index <= currentStep) Color(0xFF1C1C1C) else Color(0xFF9E9E9E), maxLines = 1)
                        }
                    }
                }
            }

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentStep) {
                    0 -> WelcomeStep()
                    1 -> PreferencesStep(
                        selectedCurrency = selectedCurrency,
                        onCurrencyChange = viewModel::setCurrency,
                        selectedTheme = selectedTheme,
                        onThemeChange = viewModel::setTheme,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsChange = viewModel::setNotificationsEnabled,
                        biometricEnabled = biometricEnabled,
                        onBiometricChange = viewModel::setBiometricEnabled
                    )
                    2 -> CategoriesStep(
                        categories = defaultCategories,
                        selectedCategories = selectedDefaultCategories,
                        onCategoryToggle = viewModel::toggleDefaultCategory
                    )
                    3 -> FinishStep(
                        saveState = saveState,
                        onComplete = viewModel::completeOnboarding,
                        onSkip = viewModel::skipOnboarding,
                        onNavigateToHome = onNavigateToHome
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    androidx.compose.material3.TextButton(
                        onClick = viewModel::previousStep
                    ) { Text("Back", fontSize = 16.sp) }
                } else {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.skipOnboarding(onNavigateToHome) }
                    ) { Text("Skip", fontSize = 16.sp) }
                }

                if (isLastStep) {
                    Button(
                        onClick = { viewModel.completeOnboarding(onNavigateToHome) },
                        enabled = saveState is com.expensetracker.presentation.viewmodel.UiState.Loading.not,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            contentColor = Color.White
                        )
                    ) {
                        if (saveState is com.expensetracker.presentation.viewmodel.UiState.Loading) {
                            ProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp).padding(end = 8.dp))
                        }
                        Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.W600)
                    }
                } else {
                    Button(
                        onClick = viewModel::nextStep,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Green600,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.W600)
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(120.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(color = Green600, center = androidx.compose.ui.geometry.Offset(60.dp.toPx(), 60.dp.toPx()), radius = 60.dp.toPx())
                drawText(text = "💰", fontSize = 60.sp, color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(30.dp.toPx(), 80.dp.toPx()))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Welcome to Expense Tracker", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C), textAlign = androidx.compose.ui.text.TextAlign.Center)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Take control of your finances with smart budgeting, insightful reports, and automated tracking.",
            fontSize = 16.sp,
            color = Color(0xFF757575),
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun PreferencesStep(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    selectedTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    biometricEnabled: Boolean,
    onBiometricChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Set your preferences", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Customize the app to match your needs", fontSize = 16.sp, color = Color(0xFF757575))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

        // Currency
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Currency", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                        Text(text = "Default currency for amounts", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) { }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD").forEach { currency ->
                        val isSelected = selectedCurrency == currency
                        androidx.compose.material3.Chip(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onClick = { onCurrencyChange(currency) },
                            selected = isSelected,
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                selectedContainerColor = Green600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEEEEE)
                            )
                        ) { Text(text = currency, fontSize = 14.sp) }
                    }
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        // Theme
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Theme", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                Text(text = "Choose your preferred appearance", fontSize = 12.sp, color = Color(0xFF757575), modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.values().forEach { theme ->
                        val isSelected = selectedTheme == theme
                        androidx.compose.material3.Chip(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onClick = { onThemeChange(theme) },
                            selected = isSelected,
                            colors = androidx.compose.material3.ChipDefaults.colors(
                                selectedContainerColor = Green600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFEEEEEE)
                            )
                        ) { Text(text = theme.name, fontSize = 14.sp) }
                    }
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        // Notifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Notifications", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                        Text(text = "Receive budget alerts and reminders", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    androidx.compose.material3.Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Green600,
                            trackColor = Green600.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

        // Biometric
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Biometric Lock", fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                        Text(text = "Secure app with fingerprint/face ID", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    androidx.compose.material3.Switch(
                        checked = biometricEnabled,
                        onCheckedChange = onBiometricChange,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            thumbColor = Green600,
                            trackColor = Green600.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesStep(
    categories: List<com.expensetracker.domain.model.Category>,
    selectedCategories: Set<Long>,
    onCategoryToggle: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Choose default categories", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Select categories to enable by default", fontSize = 16.sp, color = Color(0xFF757575))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategories.contains(category.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                    )
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onCategoryToggle(category.id) },
                        modifier = Modifier.padding(16.dp),
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = Green600,
                            uncheckedColor = Color(0xFFBDBDBD)
                        )
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.size(40.dp).background(Color(category.color).copy(alpha = 0.2f))
                            ) {
                                // Category icon placeholder
                            }
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = category.name, fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
                                Text(text = category.type.name, fontSize = 12.sp, color = Color(0xFF757575))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinishStep(
    saveState: com.expensetracker.presentation.viewmodel.UiState<Unit>,
    onComplete: (() -> Unit) -> Unit,
    onSkip: (() -> Unit) -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(120.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(color = Green600, center = androidx.compose.ui.geometry.Offset(60.dp.toPx(), 60.dp.toPx()), radius = 60.dp.toPx())
                drawText(text = "✓", fontSize = 60.sp, color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(35.dp.toPx(), 80.dp.toPx()))
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        Text(text = "You're all set!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C), textAlign = androidx.compose.ui.text.TextAlign.Center)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Expense Tracker is ready to use. Start tracking your finances today!",
            fontSize = 16.sp,
            color = Color(0xFF757575),
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}