package com.expensetracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.expensetracker.domain.model.TransactionType
import com.expensetracker.presentation.ui.screen.budget.BudgetManagementScreen
import com.expensetracker.presentation.ui.screen.category.CategoryManagementScreen
import com.expensetracker.presentation.ui.screen.home.HomeScreen
import com.expensetracker.presentation.ui.screen.onboarding.OnboardingScreen
import com.expensetracker.presentation.ui.screen.recurring.RecurringTransactionsScreen
import com.expensetracker.presentation.ui.screen.reports.ReportsScreen
import com.expensetracker.presentation.ui.screen.settings.SettingsScreen
import com.expensetracker.presentation.ui.screen.splash.SplashScreen
import com.expensetracker.presentation.ui.screen.transaction.AddEditTransactionScreen
import com.expensetracker.presentation.ui.screen.transaction.TransactionListScreen
import com.expensetracker.presentation.viewmodel.AddEditTransactionViewModel
import com.expensetracker.presentation.viewmodel.BudgetManagementViewModel
import com.expensetracker.presentation.viewmodel.CategoryManagementViewModel
import com.expensetracker.presentation.viewmodel.HomeViewModel
import com.expensetracker.presentation.viewmodel.OnboardingViewModel
import com.expensetracker.presentation.viewmodel.RecurringTransactionsViewModel
import com.expensetracker.presentation.viewmodel.ReportsViewModel
import com.expensetracker.presentation.viewmodel.SettingsViewModel
import com.expensetracker.presentation.viewmodel.SplashViewModel
import com.expensetracker.presentation.viewmodel.TransactionListViewModel

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = AppRoute.Splash) {
    NavHost(navController, startDestination) {
        // =========================================================================
        // Auth / Onboarding Routes
        // =========================================================================
        composable(AppRoute.Splash) {
            SplashScreen(
                viewModel = hiltViewModel<SplashViewModel>(),
                onNavigateToOnboarding = {
                    navController.navigate(AppRoute.Onboarding) {
                        popUpTo(AppRoute.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoute.Onboarding) {
            OnboardingScreen(
                viewModel = hiltViewModel<OnboardingViewModel>(),
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // =========================================================================
        // Main Navigation Routes (Bottom Nav)
        // =========================================================================
        composable(AppRoute.Home) {
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                onNavigateToTransactionList = { type ->
                    navController.navigateToTransactions(type)
                },
                onNavigateToAddTransaction = { type ->
                    navController.navigateToAddTransaction(type)
                },
                onNavigateToCategories = { navController.navigateToCategories() },
                onNavigateToBudgets = { navController.navigateToBudgets() },
                onNavigateToReports = { navController.navigateToReports() },
                onNavigateToRecurring = { navController.navigateToRecurring() },
                onNavigateToSettings = { navController.navigateToSettings() }
            )
        }

        composable(AppRoute.Transactions) {
            TransactionListScreen(
                viewModel = hiltViewModel<TransactionListViewModel>(),
                transactionType = TransactionType.ALL,
                onNavigateToAdd = { type ->
                    navController.navigateToAddTransaction(type)
                },
                onNavigateToEdit = { transaction ->
                    navController.navigateToEditTransaction(transaction.id!!)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Reports) {
            ReportsScreen(
                viewModel = hiltViewModel<ReportsViewModel>(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Recurring) {
            RecurringTransactionsScreen(
                viewModel = hiltViewModel<RecurringTransactionsViewModel>(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Settings) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // =========================================================================
        // Detail / Nested Routes
        // =========================================================================
        // Transaction List with type filter
        composable(
            route = AppRoute.TransactionList,
            arguments = listOf(navArgument(AppRoute.TransactionTypeArg) {
                type = NavType.StringType
                defaultValue = TransactionType.ALL.name
            })
        ) { backStackEntry ->
            val typeString = backStackEntry.getString() ?: TransactionType.ALL.name
            val type = TransactionType.fromString(typeString)
            TransactionListScreen(
                viewModel = hiltViewModel<TransactionListViewModel>(),
                transactionType = type,
                onNavigateToAdd = { tType ->
                    navController.navigateToAddTransaction(tType)
                },
                onNavigateToEdit = { transaction ->
                    navController.navigateToEditTransaction(transaction.id!!)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Add Transaction with type
        composable(
            route = AppRoute.AddTransaction,
            arguments = listOf(navArgument(AppRoute.TransactionTypeArg) {
                type = NavType.StringType
                defaultValue = TransactionType.EXPENSE.name
            })
        ) { backStackEntry ->
            val typeString = backStackEntry.getString() ?: TransactionType.EXPENSE.name
            val type = TransactionType.fromString(typeString)
            AddEditTransactionScreen(
                viewModel = hiltViewModel<AddEditTransactionViewModel>(),
                transactionType = type,
                transactionId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Transaction with ID
        composable(
            route = AppRoute.EditTransaction,
            arguments = listOf(navArgument(AppRoute.TransactionIdArg) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            AddEditTransactionScreen(
                viewModel = hiltViewModel<AddEditTransactionViewModel>(),
                transactionType = TransactionType.EXPENSE, // Will be loaded from DB
                transactionId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Category Management
        composable(AppRoute.Categories) {
            CategoryManagementScreen(
                viewModel = hiltViewModel<CategoryManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigateToAddCategory() },
                onNavigateToEditCategory = { id ->
                    navController.navigateToEditCategory(id)
                }
            )
        }

        // Add Category
        composable(AppRoute.AddCategory) {
            // CategoryEditScreen for adding - we'll use the same screen with null id
            CategoryManagementScreen(
                viewModel = hiltViewModel<CategoryManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigateToAddCategory() },
                onNavigateToEditCategory = { id ->
                    navController.navigateToEditCategory(id)
                },
                initialCategoryId = null
            )
        }

        // Edit Category with ID
        composable(
            route = AppRoute.EditCategory,
            arguments = listOf(navArgument(AppRoute.CategoryIdArg) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            CategoryManagementScreen(
                viewModel = hiltViewModel<CategoryManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigateToAddCategory() },
                onNavigateToEditCategory = { id ->
                    navController.navigateToEditCategory(id)
                },
                initialCategoryId = id
            )
        }

        // Budget Management
        composable(AppRoute.Budgets) {
            BudgetManagementScreen(
                viewModel = hiltViewModel<BudgetManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBudget = { navController.navigateToAddBudget() },
                onNavigateToEditBudget = { id ->
                    navController.navigateToEditBudget(id)
                }
            )
        }

        // Add Budget
        composable(AppRoute.AddBudget) {
            BudgetManagementScreen(
                viewModel = hiltViewModel<BudgetManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBudget = { navController.navigateToAddBudget() },
                onNavigateToEditBudget = { id ->
                    navController.navigateToEditBudget(id)
                },
                initialBudgetId = null
            )
        }

        // Edit Budget with ID
        composable(
            route = AppRoute.EditBudget,
            arguments = listOf(navArgument(AppRoute.BudgetIdArg) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            BudgetManagementScreen(
                viewModel = hiltViewModel<BudgetManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBudget = { navController.navigateToAddBudget() },
                onNavigateToEditBudget = { id ->
                    navController.navigateToEditBudget(id)
                },
                initialBudgetId = id
            )
        }

        // Reports Detail
        composable(
            route = AppRoute.ReportDetail,
            arguments = listOf(navArgument(AppRoute.ReportTypeArg) {
                type = NavType.StringType
                defaultValue = "expense_breakdown"
            })
        ) { backStackEntry ->
            val reportType = backStackEntry.getString() ?: "expense_breakdown"
            ReportsScreen(
                viewModel = hiltViewModel<ReportsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialReportType = reportType
            )
        }

        // Recurring Transactions
        composable(AppRoute.AddRecurring) {
            RecurringTransactionsScreen(
                viewModel = hiltViewModel<RecurringTransactionsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRecurring = { navController.navigateToAddRecurring() },
                onNavigateToEditRecurring = { id ->
                    navController.navigateToEditRecurring(id)
                },
                initialRecurringId = null
            )
        }

        composable(
            route = AppRoute.EditRecurring,
            arguments = listOf(navArgument(AppRoute.RecurringIdArg) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            RecurringTransactionsScreen(
                viewModel = hiltViewModel<RecurringTransactionsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRecurring = { navController.navigateToAddRecurring() },
                onNavigateToEditRecurring = { id ->
                    navController.navigateToEditRecurring(id)
                },
                initialRecurringId = id
            )
        }

        // Settings Sub-screens
        composable(AppRoute.SettingsAppearance) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "appearance"
            )
        }

        composable(AppRoute.SettingsCurrency) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "currency"
            )
        }

        composable(AppRoute.SettingsLanguage) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "language"
            )
        }

        composable(AppRoute.SettingsNotifications) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "notifications"
            )
        }

        composable(AppRoute.SettingsDataManagement) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "data"
            )
        }

        composable(AppRoute.SettingsAbout) {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                initialSection = "about"
            )
        }

        // =========================================================================
        // Deep Link Routes
        // =========================================================================
        composable(
            route = AppRoute.DeepLinkTransaction,
            arguments = listOf(navArgument(AppRoute.TransactionIdArg) {
                type = NavType.LongType
            }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "expensetracker://transaction/{transactionId}"
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            AddEditTransactionScreen(
                viewModel = hiltViewModel<AddEditTransactionViewModel>(),
                transactionType = TransactionType.EXPENSE,
                transactionId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.DeepLinkCategory,
            arguments = listOf(navArgument(AppRoute.CategoryIdArg) {
                type = NavType.LongType
            }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "expensetracker://category/{categoryId}"
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            CategoryManagementScreen(
                viewModel = hiltViewModel<CategoryManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = { navController.navigateToAddCategory() },
                onNavigateToEditCategory = { id ->
                    navController.navigateToEditCategory(id)
                },
                initialCategoryId = id
            )
        }

        composable(
            route = AppRoute.DeepLinkBudget,
            arguments = listOf(navArgument(AppRoute.BudgetIdArg) {
                type = NavType.LongType
            }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "expensetracker://budget/{budgetId}"
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            BudgetManagementScreen(
                viewModel = hiltViewModel<BudgetManagementViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBudget = { navController.navigateToAddBudget() },
                onNavigateToEditBudget = { id ->
                    navController.navigateToEditBudget(id)
                },
                initialBudgetId = id
            )
        }

        composable(
            route = AppRoute.DeepLinkRecurring,
            arguments = listOf(navArgument(AppRoute.RecurringIdArg) {
                type = NavType.LongType
            }),
            deepLinks = listOf(navDeepLink {
                uriPattern = "expensetracker://recurring/{recurringId}"
            })
        ) { backStackEntry ->
            val id = backStackEntry.getLong() ?: -1L
            RecurringTransactionsScreen(
                viewModel = hiltViewModel<RecurringTransactionsViewModel>(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRecurring = { navController.navigateToAddRecurring() },
                onNavigateToEditRecurring = { id ->
                    navController.navigateToEditRecurring(id)
                },
                initialRecurringId = id
            )
        }
    }
}

@Composable
fun AppNavHostRoot() {
    val navController = rememberNavController()
    AppNavHost(navController)
}