package com.expensetracker.presentation.ui.screen.home

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.domain.model.Transaction
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.presentation.theme.AppTheme
import com.expensetracker.presentation.theme.Green600
import com.expensetracker.presentation.ui.component.BudgetProgressCard
import com.expensetracker.presentation.ui.component.CategoryFilterChips
import com.expensetracker.presentation.ui.component.EmptyTransactionsState
import com.expensetracker.presentation.ui.component.TransactionCard
import com.expensetracker.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTransactionList: (TransactionType) -> Unit,
    onNavigateToAddTransaction: (TransactionType) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            TopAppBarSection(
                onNavigateToSettings = onNavigateToSettings
            )

            // Content
            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is com.expensetracker.presentation.viewmodel.UiState.Loading -> LoadingState()
                    is com.expensetracker.presentation.viewmodel.UiState.Error -> ErrorState(message = it.message, onRetry = viewModel::refresh)
                    is com.expensetracker.presentation.viewmodel.UiState.Success -> {
                        HomeContent(
                            data = it.data,
                            onNavigateToTransactionList = onNavigateToTransactionList,
                            onNavigateToAddTransaction = onNavigateToAddTransaction,
                            onNavigateToCategories = onNavigateToCategories,
                            onNavigateToBudgets = onNavigateToBudgets,
                            onNavigateToReports = onNavigateToReports,
                            onNavigateToRecurring = onNavigateToRecurring
                        )
                    }
                    is com.expensetracker.presentation.viewmodel.UiState.Empty -> EmptyState()
                }
            }

            // Bottom Navigation placeholder (handled by parent)
        }
    }
}

@Composable
fun TopAppBarSection(
    onNavigateToSettings: () -> Unit
) {
    androidx.compose.material3.TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = "Expense Tracker", fontSize = 20.sp, fontWeight = FontWeight.W600, color = Color(0xFF1C1C1C)) },
        actions = {
            androidx.compose.material3.IconButton(onClick = onNavigateToSettings) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF1C1C1C))
            }
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color(0xFF1C1C1C)
        )
    )
}

@Composable
fun HomeContent(
    data: HomeViewModel.HomeUiState,
    onNavigateToTransactionList: (TransactionType) -> Unit,
    onNavigateToAddTransaction: (TransactionType) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRecurring: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Card
        BalanceCard(
            totalIncome = data.totalIncome,
            totalExpense = data.totalExpense,
            balance = data.balance
        )

        // Quick Actions
        QuickActionsGrid(
            onAddExpense = { onNavigateToAddTransaction(TransactionType.Expense) },
            onAddIncome = { onNavigateToAddTransaction(TransactionType.Income) },
            onViewReports = onNavigateToReports,
            onViewBudgets = onNavigateToBudgets
        )

        // Budget Progress
        if (data.budgetProgress.isNotEmpty()) {
            BudgetSection(
                budgets = data.budgetProgress,
                onViewAll = onNavigateToBudgets
            )
        }

        // Recent Transactions
        RecentTransactionsSection(
            transactions = data.recentTransactions,
            onViewAll = { onNavigateToTransactionList(TransactionType.Expense) }
        )
    }
}

@Composable
fun BalanceCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Green600
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Total Balance", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f))
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("$%.2f", balance),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Income", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = String.format("$%.2f", totalIncome), fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Expense", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = String.format("$%.2f", totalExpense), fontSize = 16.sp, fontWeight = FontWeight.W600, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onViewReports: () -> Unit,
    onViewBudgets: () -> Unit
) {
    androidx.compose.foundation.layout.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
    ) {
        items(listOf(
            QuickAction("Add Expense", Icons.Default.ReceiptLong, Color(0xFFC62828), onAddExpense),
            QuickAction("Add Income", Icons.Default.TrendingUp, Color(0xFF1B5E20), onAddIncome),
            QuickAction("Reports", Icons.Default.PieChart, Color(0xFF1565C0), onViewReports),
            QuickAction("Budgets", Icons.Default.AccountBalanceWallet, Color(0xFFBF360C), onViewBudgets)
        )) { action ->
            QuickActionCard(action = action)
        }
    }
}

data class QuickAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier.width(160.dp).height(100.dp),
        onClick = action.onClick,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f)
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(40.dp).background(action.color.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.color,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = Color(0xFF1C1C1C),
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
        }
    }
}

@Composable
fun BudgetSection(
    budgets: List<HomeViewModel.BudgetProgress>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Budgets", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
            androidx.compose.material3.TextButton(onClick = onViewAll) { Text("View All", fontSize = 14.sp) }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(budgets) { budget ->
                BudgetProgressCard(
                    title = budget.categoryName,
                    spent = budget.spent,
                    budget = budget.budget,
                    period = "This Month"
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsSection(
    transactions: List<Transaction>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Recent Transactions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1C))
            androidx.compose.material3.TextButton(onClick = onViewAll) { Text("View All", fontSize = 14.sp) }
        }
        if (transactions.isEmpty()) {
            EmptyTransactionsState(onAddClick = { })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions.take(5)) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        onClick = { },
                        onEdit = { },
                        onDelete = { }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(color = Green600)
    }
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
        Text(text = "No data available", fontSize = 16.sp, color = Color(0xFF757575))
    }
}