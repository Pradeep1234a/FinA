package com.expensetracker.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val currency: String = "USD",
    val language: String = "en",
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val budgetAlertEnabled: Boolean = true,
    val recurringTransactionEnabled: Boolean = true,
    val onboardingComplete: Boolean = false
) {
    companion object {
        val SUPPORTED_CURRENCIES = listOf(
            "USD", "EUR", "GBP", "JPY", "INR", "CAD", "AUD", "CHF", "CNY", "KRW"
        )

        val SUPPORTED_LANGUAGES = listOf(
            "en", "es", "fr", "de", "zh", "ja", "ko", "pt", "ru", "hi"
        )

        fun copy(
            settings: Settings,
            theme: ThemeMode? = null,
            currency: String? = null,
            language: String? = null,
            firstDayOfWeek: DayOfWeek? = null,
            budgetAlertEnabled: Boolean? = null,
            recurringTransactionEnabled: Boolean? = null,
            onboardingComplete: Boolean? = null
        ): Settings {
            return settings.copy(
                theme = theme ?: settings.theme,
                currency = currency ?: settings.currency,
                language = language ?: settings.language,
                firstDayOfWeek = firstDayOfWeek ?: settings.firstDayOfWeek,
                budgetAlertEnabled = budgetAlertEnabled ?: settings.budgetAlertEnabled,
                recurringTransactionEnabled = recurringTransactionEnabled ?: settings.recurringTransactionEnabled,
                onboardingComplete = onboardingComplete ?: settings.onboardingComplete
            )
        }
    }
}