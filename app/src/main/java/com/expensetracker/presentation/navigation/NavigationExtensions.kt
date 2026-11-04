package com.expensetracker.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHostController
import androidx.navigation.compose.getString
import androidx.navigation.compose.getInt
import androidx.navigation.compose.getLong
import androidx.navigation.compose.popUpTo
import com.expensetracker.domain.model.TransactionType

/**
 * Navigation extension functions for type-safe navigation
 */
fun NavController.navigateToSplash() {
    navigate(AppRoute.Splash) {
        popUpTo(AppRoute.Splash) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToOnboarding() {
    navigate(AppRoute.Onboarding) {
        popUpTo(AppRoute.Splash) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToHome() {
    navigate(AppRoute.Home) {
        popUpTo(AppRoute.Onboarding) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateToTransactions(type: TransactionType = TransactionType.ALL) {
    navigate(AppRoute.transactionListRoute(type)) {
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToAddTransaction(type: TransactionType) {
    navigate(AppRoute.addTransactionRoute(type)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToEditTransaction(id: Long) {
    navigate(AppRoute.editTransactionRoute(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToCategories() {
    navigate(AppRoute.Categories) {
        launchSingleTop = true
    }
}

fun NavController.navigateToAddCategory() {
    navigate(AppRoute.AddCategory) {
        launchSingleTop = true
    }
}

fun NavController.navigateToEditCategory(id: Long) {
    navigate(AppRoute.editCategoryRoute(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToBudgets() {
    navigate(AppRoute.Budgets) {
        launchSingleTop = true
    }
}

fun NavController.navigateToAddBudget() {
    navigate(AppRoute.AddBudget) {
        launchSingleTop = true
    }
}

fun NavController.navigateToEditBudget(id: Long) {
    navigate(AppRoute.editBudgetRoute(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToReports() {
    navigate(AppRoute.Reports) {
        launchSingleTop = true
    }
}

fun NavController.navigateToReportDetail(reportType: String) {
    navigate(AppRoute.reportDetailRoute(reportType)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToRecurring() {
    navigate(AppRoute.Recurring) {
        launchSingleTop = true
    }
}

fun NavController.navigateToAddRecurring() {
    navigate(AppRoute.AddRecurring) {
        launchSingleTop = true
    }
}

fun NavController.navigateToEditRecurring(id: Long) {
    navigate(AppRoute.editRecurringRoute(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettings() {
    navigate(AppRoute.Settings) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsAppearance() {
    navigate(AppRoute.SettingsAppearance) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsCurrency() {
    navigate(AppRoute.SettingsCurrency) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsLanguage() {
    navigate(AppRoute.SettingsLanguage) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsNotifications() {
    navigate(AppRoute.SettingsNotifications) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsDataManagement() {
    navigate(AppRoute.SettingsDataManagement) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettingsAbout() {
    navigate(AppRoute.SettingsAbout) {
        launchSingleTop = true
    }
}

/**
 * Navigate to deep link destinations
 */
fun NavController.navigateToTransactionDetail(id: Long) {
    navigate(AppRoute.deepLinkTransaction(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToCategoryDetail(id: Long) {
    navigate(AppRoute.deepLinkCategory(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToBudgetDetail(id: Long) {
    navigate(AppRoute.deepLinkBudget(id)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToRecurringDetail(id: Long) {
    navigate(AppRoute.deepLinkRecurring(id)) {
        launchSingleTop = true
    }
}

/**
 * Pop back stack with options
 */
fun NavController.popBackStackWithOptions(
    inclusive: Boolean = false,
    saveState: Boolean = true
) {
    popBackStack(inclusive, saveState)
}

/**
 * Navigate up to a specific destination
 */
fun NavController.navigateUpTo(route: String, inclusive: Boolean = false) {
    popUpTo(route) { this.inclusive = inclusive }
}

/**
 * Clear back stack and navigate to destination
 */
fun NavController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Get transaction type from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getTransactionType(): TransactionType {
    val typeString = getString(AppRoute.TransactionTypeArg) ?: TransactionType.ALL.name
    return TransactionType.fromString(typeString)
}

/**
 * Get transaction ID from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getTransactionId(): Long {
    return getLong(AppRoute.TransactionIdArg) ?: -1L
}

/**
 * Get category ID from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getCategoryId(): Long {
    return getLong(AppRoute.CategoryIdArg) ?: -1L
}

/**
 * Get budget ID from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getBudgetId(): Long {
    return getLong(AppRoute.BudgetIdArg) ?: -1L
}

/**
 * Get recurring transaction ID from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getRecurringId(): Long {
    return getLong(AppRoute.RecurringIdArg) ?: -1L
}

/**
 * Get report type from navigation arguments
 */
fun androidx.navigation.NavBackStackEntry.getReportType(): String {
    return getString(AppRoute.ReportTypeArg) ?: "expense_breakdown"
}

/**
 * Check if current destination is a bottom nav destination
 */
fun NavController.isAtBottomNavDestination(): Boolean {
    val currentRoute = currentBackStackEntry?.destination?.route
    return currentRoute != null && AppRoute.BottomNavItem.fromRoute(currentRoute) != null
}

/**
 * Get current bottom nav item
 */
fun NavController.getCurrentBottomNavItem(): AppRoute.BottomNavItem? {
    val currentRoute = currentBackStackEntry?.destination?.route
    return currentRoute?.let { AppRoute.BottomNavItem.fromRoute(it) }
}

/**
 * Select bottom nav item programmatically
 */
fun NavController.selectBottomNavItem(item: AppRoute.BottomNavItem) {
    if (!isAtBottomNavDestination() || getCurrentBottomNavItem() != item) {
        navigate(item.route) {
            popUpTo(AppRoute.Home) { inclusive = true }
            launchSingleTop = true
        }
    }
}

/**
 * Navigate with custom options
 */
fun NavController.navigateWithOptions(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    launchSingleTop: Boolean = false,
    restoreState: Boolean = false
) {
    navigate(route) {
        popUpToRoute?.let { popUpTo(it) { this.inclusive = inclusive } }
        this.launchSingleTop = launchSingleTop
        this.restoreState = restoreState
    }
}

/**
 * Safe navigate - handles null navController
 */
fun NavController?.safeNavigate(
    route: String,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    this?.let { navController ->
        navController.navigate(route, navOptionsBuilder)
    }
}

/**
 * NavOptions builder for common patterns
 */
class NavOptionsBuilder {
    var popUpToRoute: String? = null
    var inclusive: Boolean = false
    var launchSingleTop: Boolean = false
    var restoreState: Boolean = false

    fun popUpTo(route: String, inclusive: Boolean = false) {
        popUpToRoute = route
        this.inclusive = inclusive
    }

    fun singleTop() {
        launchSingleTop = true
    }

    fun restore() {
        restoreState = true
    }
}

inline fun NavOptionsBuilder(block: NavOptionsBuilder.() -> Unit): NavOptionsBuilder {
    val builder = NavOptionsBuilder()
    builder.block()
    return builder
}