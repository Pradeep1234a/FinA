package com.expensetracker.presentation.ui.screen.settings

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filed.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.expensetracker.presentation.ui.component.DatePicker
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val backupFrequency by viewModel.backupFrequency.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            androidx.compose.material3.TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1C1C1C))
                    }
                },
                title = { Text(text = "Settings", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1C)
                )
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Appearance Section
                    item {
                        SettingsSection(title = "Appearance") {
                            SettingItem(
                                title = "Theme",
                                subtitle = themeMode.name,
                                icon = Icons.Default.Palette,
                                onClick = { showThemeDialog() }
                            )
                        }
                    }

                    // General Section
                    item {
                        SettingsSection(title = "General") {
                            SettingItem(
                                title = "Currency",
                                subtitle = currency,
                                icon = Icons.Default.AttachMoney,
                                onClick = { showCurrencyDialog = true }
                            )
                            SettingItem(
                                title = "Language",
                                subtitle = when (language) { "en" -> "English"; "es" -> "Spanish"; "fr" -> "French"; else -> language },
                                icon = Icons.Default.Language,
                                onClick = { showLanguageDialog = true }
                            )
                        }
                    }

                    // Security Section
                    item {
                        SettingsSection(title = "Security") {
                            SettingItem(
                                title = "Notifications",
                                subtitle = "Budget alerts and reminders",
                                icon = Icons.Default.Notifications,
                                trailing = {
                                    Switch(
                                        checked = notificationsEnabled,
                                        onCheckedChange = viewModel::updateNotificationsEnabled,
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            thumbColor = Green600,
                                            trackColor = Green600.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                            SettingItem(
                                title = "Biometric Lock",
                                subtitle = "Secure with fingerprint or Face ID",
                                icon = Icons.Default.Fingerprint,
                                trailing = {
                                    Switch(
                                        checked = biometricEnabled,
                                        onCheckedChange = viewModel::updateBiometricEnabled,
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            thumbColor = Green600,
                                            trackColor = Green600.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Backup Section
                    item {
                        SettingsSection(title = "Backup & Restore") {
                            SettingItem(
                                title = "Auto Backup",
                                subtitle = "Automatically backup data to cloud",
                                icon = Icons.Default.CloudUpload,
                                trailing = {
                                    Switch(
                                        checked = autoBackupEnabled,
                                        onCheckedChange = viewModel::updateAutoBackupEnabled,
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            thumbColor = Green600,
                                            trackColor = Green600.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                            SettingItem(
                                title = "Backup Frequency",
                                subtitle = backupFrequency.name,
                                icon = Icons.Default.CloudUpload,
                                onClick = { showBackupFrequencyDialog() }
                            )
                            SettingItem(
                                title = "Create Backup Now",
                                subtitle = "Export all data to file",
                                icon = Icons.Default.Backup,
                                onClick = { showBackupDialog = true }
                            )
                            SettingItem(
                                title = "Restore Backup",
                                subtitle = "Import data from backup file",
                                icon = Icons.Default.CloudDownload,
                                onClick = { showRestoreDialog = true }
                            )
                        }
                    }

                    // Data Section
                    item {
                        SettingsSection(title = "Data") {
                            SettingItem(
                                title = "Export Data",
                                subtitle = "Export transactions as CSV, JSON, or PDF",
                                icon = Icons.Default.CloudUpload,
                                onClick = { showExportDialog = true }
                            )
                            SettingItem(
                                title = "Import Data",
                                subtitle = "Import transactions from file",
                                icon = Icons.Default.CloudDownload,
                                onClick = { showImportDialog = true }
                            )
                        }
                    }

                    // About Section
                    item {
                        SettingsSection(title = "About") {
                            SettingItem(
                                title = "Version",
                                subtitle = "1.0.0",
                                icon = Icons.Default.Info,
                                onClick = { }
                            )
                            SettingItem(
                                title = "Privacy Policy",
                                subtitle = "Read our privacy policy",
                                icon = Icons.Default.Info,
                                onClick = { }
                            )
                            SettingItem(
                                title = "Terms of Service",
                                subtitle = "Read our terms of service",
                                icon = Icons.Default.Info,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }

    // Theme Dialog
    ThemeDialog(
        selectedTheme = themeMode,
        onThemeSelect = { theme ->
            viewModel.updateThemeMode(theme)
        },
        onDismiss = { }
    )

    // Currency Dialog
    if (showCurrencyDialog) {
        CurrencyDialog(
            selectedCurrency = currency,
            onCurrencySelect = { curr ->
                viewModel.updateCurrency(curr)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            selectedLanguage = language,
            onLanguageSelect = { lang ->
                viewModel.updateLanguage(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Backup Frequency Dialog
    if (showBackupDialog) {
        BackupFrequencyDialog(
            selectedFrequency = backupFrequency,
            onFrequencySelect = { freq ->
                viewModel.updateBackupFrequency(freq)
            },
            onDismiss = { showBackupDialog = false }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onFormatSelect = { format ->
                viewModel.exportData(format) { path ->
                    // Show success message
                    showExportDialog = false
                }
            },
            onDismiss = { showExportDialog = false }
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            onCreateBackup = { path ->
                // Show success
                showBackupDialog = false
            },
            onDismiss = { showBackupDialog = false }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        RestoreDialog(
            onRestore = { path ->
                viewModel.restoreBackup(path) { }
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val isClickable = onClick != null

    androidx.compose.material3.TextButton(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        enabled = isClickable
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF757575))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, color = if (isClickable) Color(0xFF1C1C1C) else Color(0xFF757575))
                    Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                }
            }
            if (trailing != null) {
                trailing()
            } else if (isClickable) {
                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF9E9E9E))
            }
        }
    }
}

@Composable
fun ThemeDialog(
    selectedTheme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Theme", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { theme ->
                    val isSelected = selectedTheme == theme
                    androidx.compose.material3.TextButton(
                        onClick = { onThemeSelect(theme) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = theme.name, fontSize = 16.sp, color = if (isSelected) Green600 else Color(0xFF1C1C1C))
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Green600)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun CurrencyDialog(
    selectedCurrency: String,
    onCurrencySelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Currency", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "BRL").forEach { currency ->
                    val isSelected = selectedCurrency == currency
                    androidx.compose.material3.TextButton(
                        onClick = { onCurrencySelect(currency) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = currency, fontSize = 16.sp, color = if (isSelected) Green600 else Color(0xFF1C1C1C))
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Green600)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun LanguageDialog(
    selectedLanguage: String,
    onLanguageSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Language", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mapOf("en" to "English", "es" to "Spanish", "fr" to "French", "de" to "German", "zh" to "Chinese", "ja" to "Japanese", "ko" to "Korean", "pt" to "Portuguese", "ru" to "Russian", "ar" to "Arabic").forEach { (code, name) ->
                    val isSelected = selectedLanguage == code
                    androidx.compose.material3.TextButton(
                        onClick = { onLanguageSelect(code) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, fontSize = 16.sp, color = if (isSelected) Green600 else Color(0xFF1C1C1C))
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Green600)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun BackupFrequencyDialog(
    selectedFrequency: SettingsViewModel.BackupFrequency,
    onFrequencySelect: (SettingsViewModel.BackupFrequency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Backup Frequency", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsViewModel.BackupFrequency.values().forEach { freq ->
                    val isSelected = selectedFrequency == freq
                    androidx.compose.material3.TextButton(
                        onClick = { onFrequencySelect(freq) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = freq.name, fontSize = 16.sp, color = if (isSelected) Green600 else Color(0xFF1C1C1C))
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Green600)
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun ExportDialog(
    onFormatSelect: (SettingsViewModel.ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Export Format", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsViewModel.ExportFormat.values().forEach { format ->
                    androidx.compose.material3.TextButton(
                        onClick = { onFormatSelect(format) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = format.name, fontSize = 16.sp, color = Color(0xFF1C1C1C))
                        }
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun BackupDialog(
    onCreateBackup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create Backup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "This will create a complete backup of all your data including transactions, categories, budgets, and settings.", fontSize = 14.sp, color = Color(0xFF757575))
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateBackup("") },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Green600,
                    contentColor = Color.White
                )
            ) { Text(text = "Create Backup", fontSize = 14.sp, fontWeight = FontWeight.W600) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun RestoreDialog(
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Restore Backup", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "This will replace all your current data with the backup data. This action cannot be undone.", fontSize = 14.sp, color = Color(0xFFC62828), fontWeight = FontWeight.W600)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Select a backup file to restore from.", fontSize = 14.sp, color = Color(0xFF757575))
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestore("") },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White
                )
            ) { Text(text = "Restore", fontSize = 14.sp, fontWeight = FontWeight.W600) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}