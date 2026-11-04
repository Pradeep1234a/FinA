package com.expensetracker.util

import android.content.Context
import com.expensetracker.domain.model.Settings
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object CurrencyUtils {

    private val CURRENCY_SYMBOLS = mapOf(
        "USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥",
        "INR" to "₹", "CAD" to "CA$", "AUD" to "A$", "CHF" to "CHF",
        "CNY" to "¥", "KRW" to "₩"
    )

    private val CURRENCY_NAMES = mapOf(
        "USD" to "US Dollar", "EUR" to "Euro", "GBP" to "British Pound",
        "JPY" to "Japanese Yen", "INR" to "Indian Rupee", "CAD" to "Canadian Dollar",
        "AUD" to "Australian Dollar", "CHF" to "Swiss Franc",
        "CNY" to "Chinese Yuan", "KRW" to "South Korean Won"
    )

    private val CURRENCY_DECIMAL_DIGITS = mapOf(
        "JPY" to 0, "KRW" to 0, "VND" to 0, "IDR" to 0
    )

    private val EXCHANGE_RATES_USD = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 150.0,
        "INR" to 83.0, "CAD" to 1.36, "AUD" to 1.53, "CHF" to 0.90,
        "CNY" to 7.24, "KRW" to 1320.0
    )

    fun format(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        val decimalDigits = getDecimalDigits(currencyCode)
        return formatAmount(amount, symbol, decimalDigits)
    }

    fun format(amount: Double, settings: Settings): String {
        return format(amount, settings.currency)
    }

    private fun formatAmount(amount: Double, symbol: String, decimalDigits: Int): String {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        symbols.groupingSeparator = ','

        val pattern = if (decimalDigits > 0) "#,##0." + "0".repeat(decimalDigits) else "#,##0"
        val format = DecimalFormat(pattern, symbols)
        format.roundingMode = RoundingMode.HALF_UP
        return "$symbol${format.format(amount)}"
    }

    fun formatWithCode(amount: Double, currencyCode: String): String {
        val decimalDigits = getDecimalDigits(currencyCode)
        val formatted = formatAmount(amount, "", decimalDigits)
        return "$currencyCode $formatted"
    }

    fun formatForInput(amount: Double, currencyCode: String): String {
        val decimalDigits = getDecimalDigits(currencyCode)
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        symbols.groupingSeparator = ''

        val pattern = if (decimalDigits > 0) "0." + "0".repeat(decimalDigits) else "0"
        val format = DecimalFormat(pattern, symbols)
        format.roundingMode = RoundingMode.HALF_UP
        return format.format(amount)
    }

    fun getSymbol(currencyCode: String): String {
        return CURRENCY_SYMBOLS[currencyCode.uppercase()] ?: currencyCode
    }

    fun getName(currencyCode: String): String {
        return CURRENCY_NAMES[currencyCode.uppercase()] ?: currencyCode
    }

    fun getDecimalDigits(currencyCode: String): Int {
        return CURRENCY_DECIMAL_DIGITS[currencyCode.uppercase()] ?: 2
    }

    fun getExchangeRate(currencyCode: String): Double {
        return EXCHANGE_RATES_USD[currencyCode.uppercase()] ?: 1.0
    }

    fun getSupportedCurrencies(): List<String> {
        return CURRENCY_SYMBOLS.keys.toList().sorted()
    }

    fun getCurrencyDisplayMap(): Map<String, String> {
        return CURRENCY_SYMBOLS.keys.associateWith { code ->
            "${getName(code)} ($code) - ${getSymbol(code)}"
        }
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        val fromRate = getExchangeRate(fromCurrency)
        val toRate = getExchangeRate(toCurrency)
        val usdAmount = amount / fromRate
        return usdAmount * toRate
    }

    fun toUsd(amount: Double, currencyCode: String): Double {
        val rate = getExchangeRate(currencyCode)
        return if (rate > 0) amount / rate else amount
    }

    fun fromUsd(usdAmount: Double, currencyCode: String): Double {
        val rate = getExchangeRate(currencyCode)
        return usdAmount * rate
    }

    fun parse(amountString: String, currencyCode: String): Double? {
        val symbol = getSymbol(currencyCode)
        val cleaned = amountString
            .replace(symbol, "")
            .replace(currencyCode, "")
            .replace(",", "")
            .trim()
        return try { cleaned.toDouble() } catch (e: NumberFormatException) { null }
    }

    fun parseInput(input: String): Double? {
        val cleaned = input.replace(",", "").trim()
        return try { cleaned.toDouble() } catch (e: NumberFormatException) { null }
    }

    fun isSupported(currencyCode: String): Boolean {
        return CURRENCY_SYMBOLS.containsKey(currencyCode.uppercase())
    }

    fun isValidAmount(amount: Double, currencyCode: String): Boolean {
        val decimalDigits = getDecimalDigits(currencyCode)
        if (decimalDigits == 0) {
            return amount == amount.toLong().toDouble()
        }
        val factor = 10.0.pow(decimalDigits)
        val rounded = (amount * factor).roundToLong() / factor
        return amount == rounded
    }

    fun Double.toCurrency(currencyCode: String): String = format(this, currencyCode)
    fun Double.toCurrency(settings: Settings): String = format(this, settings.currency)
    fun Long.toCurrency(currencyCode: String): String = format(this.toDouble(), currencyCode)
}