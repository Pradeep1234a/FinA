package com.expensetracker.presentation.ui.screen.reports

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
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
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
import com.expensetracker.domain.model.Category
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.CategoryFilterChips
import com.expensetracker.presentation.ui.component.ChartComponents
import com.expensetracker.presentation.ui.component.DatePicker
import com.expensetracker.presentation.ui.component.EmptyState
import com.expensetracker.presentation.ui.component.LoadingState
import com.expensetracker.presentation.viewmodel.ReportsViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val expenseBreakdown by viewModel.expenseBreakdown.collectAsState()
    val incomeBreakdown by viewModel.incomeBreakdown.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showPeriodDialog by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

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
                title = { Text(text = "Reports", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
                actions = {
                    androidx.compose.material3.IconButton(onClick = { showPeriodDialog = true }) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Select Period", tint = Color(0xFF1C1C1C))
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1C1C1C)
                )
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is com.expensetracker.presentation.viewmodel.UiState.Loading -> LoadingState()
                    is com.expensetracker.presentation.viewmodel.UiState.Error -> {
                        ErrorState(message = it.message, onRetry = viewModel::loadReportData)
                    }
                    is com.expensetracker.presentation.viewmodel.UiState.Success -> {
                        ReportsContent(
                            selectedPeriod = selectedPeriod,
                            expenseBreakdown = expenseBreakdown,
                            incomeBreakdown = incomeBreakdown,
                            monthlyTrend = monthlyTrend,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            balance = balance,
                            onPeriodClick = { showPeriodDialog = true }
                        )
                    }
                    is com.expensetracker.presentation.viewmodel.UiState.Empty -> EmptyState()
                }
            }
        }
    }

    // Period Selection Dialog
    if (showPeriodDialog) {
        PeriodDialog(
            selectedPeriod = selectedPeriod,
            onPeriodSelect = { period ->
                viewModel.setPeriod(period)
                showPeriodDialog = false
            },
            onCustomSelect = {
                showPeriodDialog = false
                showCustomDatePicker = true
            },
            onDismiss = { showPeriodDialog = false }
        )
    }

    // Custom Date Picker
    if (showCustomDatePicker) {
        DateRangePicker(
            startDate = LocalDate.now().minusMonths(1),
            endDate = LocalDate.now(),
            onStartDateSelected = { start ->
                // Handle start date
            },
            onEndDateSelected = { end ->
                viewModel.setCustomDateRange(
                    start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                showCustomDatePicker = false
            },
            onDismiss = { showCustomDatePicker = false }
        )
    }
}

@Composable
fun ReportsContent(
    selectedPeriod: ReportsViewModel.Period,
    expenseBreakdown: List<Pair<String, Double>>,
    incomeBreakdown: List<Pair<String, Double>>,
    monthlyTrend: List<Pair<String, Double>>,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    onPeriodClick: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        SummaryRow(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance
        )

        // Period Selector
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onClick = onPeriodClick
        )

        // Expense Breakdown Pie Chart
        if (expenseBreakdown.isNotEmpty()) {
            ChartComponents.ExpensePieChart(
                data = expenseBreakdown,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        }

        // Income Breakdown
        if (incomeBreakdown.isNotEmpty()) {
            ChartComponents.CategoryBarChart(
                data = incomeBreakdown,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            )
        }

        // Monthly Trend
        if (monthlyTrend.isNotEmpty()) {
            ChartComponents.TrendLineChart(
                data = monthlyTrend,
                modifier = Modifier.fillMaxWidth().height(250.dp)
            )
        }

        // Category Details
        CategoryDetailsSection(
            expenseBreakdown = expenseBreakdown,
            incomeBreakdown = incomeBreakdown
        )
    }
}

@Composable
fun SummaryRow(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Income",
            amount = totalIncome,
            color = Color(0xFF1B5E20),
            icon = Icons.Default.TrendingUp
        )
        SummaryCard(
            title = "Expense",
            amount = totalExpense,
            color = Color(0xFFB71C1C),
            icon = Icons.Default.TrendingUp
        )
        SummaryCard(
            title = "Balance",
            amount = balance,
            color = if (balance >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C),
            icon = Icons.Default.PieChart
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.weight(1f),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.15f))
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.align(Alignment.Center))
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp, color = Color(0xFF757575))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("$%.2f", amount),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1C)
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: ReportsViewModel.Period,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.material3.TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Period: ${selectedPeriod.name}", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF757575))
            }
        }
    }
}

@Composable
fun CategoryDetailsSection(
    expenseBreakdown: List<Pair<String, Double>>,
    incomeBreakdown: List<Pair<String, Double>>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (expenseBreakdown.isNotEmpty()) {
            Text(text = "Top Expense Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C), modifier = Modifier.padding(start = 4.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenseBreakdown.take(5)) { (category, amount) ->
                    CategoryDetailItem(
                        name = category,
                        amount = amount,
                        color = Color(0xFFB71C1C)
                    )
                }
            }
        }

        if (incomeBreakdown.isNotEmpty()) {
            Text(text = "Income Sources", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C), modifier = Modifier.padding(start = 4.dp, top = 16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(incomeBreakdown) { (category, amount) ->
                    CategoryDetailItem(
                        name = category,
                        amount = amount,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDetailItem(
    name: String,
    amount: Double,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C))
            Text(text = String.format("$%.2f", amount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PeriodDialog(
    selectedPeriod: ReportsViewModel.Period,
    onPeriodSelect: (ReportsViewModel.Period) -> Unit,
    onCustomSelect: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Period", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportsViewModel.Period.values().forEach { period ->
                    if (period != ReportsViewModel.Period.Custom) {
                        val isSelected = selectedPeriod == period
                        androidx.compose.material3.TextButton(
                            onClick = { onPeriodSelect(period) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = period.name, fontSize = 16.sp, color = if (isSelected) Green600 else Color(0xFF1C1C1C))
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Green600)
                                }
                            }
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.layout.Divider(color = Color(0xFFEEEEEE))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.TextButton(
                    onClick = onCustomSelect,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Custom Range", fontSize = 16.sp, color = Color(0xFF1C1C1C))
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF757575))
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
fun DateRangePicker(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Custom Date Range", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C)) },
        text = {
            Column(
                modifier = Modifier.padding(24.dp).width(280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatePicker.DateRangePicker(
                    startDate = startDate,
                    endDate = endDate,
                    onStartDateSelected = onStartDateSelected,
                    onEndDateSelected = onEndDateSelected
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Green600, contentColor = Color.White)) { Text(text = "Apply", fontSize = 14.sp, fontWeight = FontWeight.W600) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(text = "Cancel", fontSize = 14.sp) }
        }
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(64.dp))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Error", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, fontSize = 16.sp, color = Color(0xFF757575), textAlign = androidx.compose.ui.text.TextAlign.Center)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyState() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(80.dp))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No data available", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Add transactions to see reports", fontSize = 16.sp, color = Color(0xFF757575))
    }
}