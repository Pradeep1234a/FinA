package com.expensetracker.presentation.navigation

import androidx.navigation.NavType
import kotlinx.datetime.LocalDate

/**
 * Navigation route definitions for the Expense Tracker app.
 * Provides type-safe navigation routes with arguments.
 */
object AppRoute {

    // =========================================================================
    // Auth / Onboarding Routes
    // =========================================================================
    const val Splash = "splash"
    const val Onboarding = "onboarding"

    // =========================================================================
    // Main Navigation Routes (Bottom Nav)
    // =========================================================================
    const val Home = "home"
    const val Transactions = "transactions"
    const val Reports = "reports"
    const val Recurring = "recurring"
    const val Settings = "settings"

    // =========================================================================
    // Detail / Nested Routes
    // =========================================================================
    // Transaction List with type filter
    const val TransactionList = "transaction_list/{transactionType}"
    const val TransactionTypeArg = "transactionType"

    // Add Transaction with type
    const val AddTransaction = "add_transaction/{transactionType}"
    // Edit Transaction with ID
    const val EditTransaction = "edit_transaction/{transactionId}"
    const val TransactionIdArg = "transactionId"

    // Category Management
    const val Categories = "categories"
    // Add/Edit Category
    const val AddCategory = "add_category"
    const val EditCategory = "edit_category/{categoryId}"
    const val CategoryIdArg = "categoryId"

    // Budget Management
    const val Budgets = "budgets"
    // Add/Edit Budget
    const val AddBudget = "add_budget"
    const val EditBudget = "edit_budget/{budgetId}"
    const val BudgetIdArg = "budgetId"

    // Reports Detail
    const val ReportDetail = "report_detail/{reportType}"
    const val ReportTypeArg = "reportType"

    // Recurring Transactions
    const val AddRecurring = "add_recurring"
    const val EditRecurring = "edit_recurring/{recurringId}"
    const val RecurringIdArg = "recurringId"

    // Settings Sub-screens
    const val SettingsAppearance = "settings_appearance"
    const val SettingsCurrency = "settings_currency"
    const val SettingsLanguage = "settings_language"
    const val SettingsNotifications = "settings_notifications"
    const val SettingsDataManagement = "settings_data_management"
    const val SettingsAbout = "settings_about"

    // =========================================================================
    // Deep Link Routes
    // =========================================================================
    const val DeepLinkTransaction = "transaction/{transactionId}"
    const val DeepLinkCategory = "category/{categoryId}"
    const val DeepLinkBudget = "budget/{budgetId}"
    const val DeepLinkRecurring = "recurring/{recurringId}"

    // =========================================================================
    // Navigation Route Builders (Type-safe navigation)
    // =========================================================================

    /**
     * Builds route for Transaction List with transaction type filter
     */
    fun transactionListRoute(type: TransactionType): String {
        return "transaction_list/$type"
    }

    /**
     * Builds route for Add Transaction with type
     */
    fun addTransactionRoute(type: TransactionType): String {
        return "add_transaction/$type"
    }

    /**
     * Builds route for Edit Transaction with ID
     */
    fun editTransactionRoute(id: Long): String {
        return "edit_transaction/$id"
    }

    /**
     * Builds route for Edit Category with ID
     */
    fun editCategoryRoute(id: Long): String {
        return "edit_category/$id"
    }

    /**
     * Builds route for Edit Budget with ID
     */
    fun editBudgetRoute(id: Long): String {
        return "edit_budget/$id"
    }

    /**
     * Builds route for Edit Recurring Transaction with ID
     */
    fun editRecurringRoute(id: Long): String {
        return "edit_recurring/$id"
    }

    /**
     * Builds route for Report Detail
     */
    fun reportDetailRoute(reportType: String): String {
        return "report_detail/$reportType"
    }

    /**
     * Builds deep link route for Transaction
     */
    fun deepLinkTransaction(id: Long): String {
        return "transaction/$id"
    }

    /**
     * Builds deep link route for Category
     */
    fun deepLinkCategory(id: Long): String {
        return "category/$id"
    }

    /**
     * Builds deep link route for Budget
     */
    fun deepLinkBudget(id: Long): String {
        return "budget/$id"
    }

    /**
     * Builds deep link route for Recurring Transaction
     */
    fun deepLinkRecurring(id: Long): String {
        return "recurring/$id"
    }

    // =========================================================================
    // Bottom Navigation Items
    // =========================================================================
    enum class BottomNavItem(
        val route: String,
        val icon: Int,
        val labelRes: Int,
        val order: Int
    ) {
        HOME(
            route = Home,
            icon = android.R.drawable.ic_menu_home,
            labelRes = R.string.nav_home,
            order = 0
        ),
        TRANSACTIONS(
            route = Transactions,
            icon = android.R.drawable.ic_menu_recent_history,
            labelRes = R.string.nav_transactions,
            order = 1
        ),
        REPORTS(
            route = Reports,
            icon = android.R.drawable.ic_menu_report_image,
            labelRes = R.string.nav_reports,
            order = 2
        ),
        RECURRING(
            route = Recurring,
            icon = android.R.drawable.ic_menu_revert,
            labelRes = R.string.nav_recurring,
            order = 3
        ),
        SETTINGS(
            route = Settings,
            icon = android.R.drawable.ic_menu_preferences,
            labelRes = R.string.nav_settings,
            order = 4
        );

        companion object {
            fun fromRoute(route: String): BottomNavItem? {
                return values().firstOrNull { it.route == route }
            }

            fun getDefault(): BottomNavItem = HOME
        }
    }
}

/**
 * Transaction type for navigation arguments
 */
enum class TransactionType(val name: String) {
    EXPENSE("expense"),
    INCOME("income"),
    ALL("all");

    companion object {
        fun fromString(value: String): TransactionType {
            return values().firstOrNull { it.name == value } ?: ALL
        }
    }
}

/**
 * Report type for navigation arguments
 */
enum class ReportType(val name: String) {
    EXPENSE_BREAKDOWN("expense_breakdown"),
    INCOME_BREAKDOWN("income_breakdown"),
    INCOME_VS_EXPENSE("income_vs_expense"),
    TREND("trend"),
    CASH_FLOW("cash_flow");

    companion object {
        fun fromString(value: String): ReportType {
            return values().firstOrNull { it.name == value } ?: EXPENSE_BREAKDOWN
        }
    }
}