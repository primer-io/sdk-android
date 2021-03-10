package io.primer.android.ui

import io.primer.android.logging.Logger
import java.util.*
import kotlin.math.min

private val INVALID_CHARACTER = Regex("[^0-9/]")
private val ZERO_OR_ONE = Regex("^[01]$")
private val BEGINS_WITH_ZERO_OR_ONE = Regex("^[01].*")
private val TWO_THROUGH_NINE = Regex("^[2-9]$")
private val THREE_OR_FOUR_DIGITS = Regex("^[0-9]{3,4}$")
private val SEPARATOR_CHAR = Regex("[\\s/-]+")
private val INVALID_MONTH = Regex("^1[3-9].*")

internal class ExpiryDateFormatter(
  private var month: String = "",
  private var year: String = "",
  private var separator: Boolean = false,
) {

    private val log = Logger("expiry-date")

    override fun toString(): String {
        return buildString {
            if (month.isNotEmpty()) {
                append(month)
            }

            if (separator) {
                append("/")
            }

            if (year.isNotEmpty()) {
                append(year)
            }
        }
    }

    fun isValid(): Boolean {
        if (month.length < 2) {
            return false
        }

        if (year.length < 2) {
            return false
        }

        val now = getDate()
        val maxMonth = month.replace("0", "").toInt() - 1
        val maxYear = getYear(now).toInt()
        val max = getDate(maxMonth, maxYear)

        return now < max
    }

    fun isEmpty(): Boolean {
        return month.isEmpty() && year.isEmpty()
    }

    fun getMonth(): String {
        return month
    }

    fun getYear(date: Calendar? = null): String {
        val now = date ?: getDate()
        val prefix = now.get(Calendar.YEAR).toString().substring(0, 2)
        return "${prefix}${year}"
    }

    companion object {

        fun fromString(value: String, autoInsert: Boolean = false): ExpiryDateFormatter {
            val tokens = tokenize(value)
            val exp = ExpiryDateFormatter()

            if (tokens.isEmpty()) {
                return exp
            }

            if (tokens.isNotEmpty()) {
                exp.month = parseMonth(tokens[0], autoInsert)
            }

            if (tokens.size > 1) {
                exp.year = parseYear(tokens[1])
            }

            if (exp.year.isNotEmpty() || (autoInsert && exp.month.length == 2)) {
                exp.separator = true
            }

            return exp
        }

        private fun getDate(month: Int? = null, year: Int? = null): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (month != null) {
                    set(Calendar.MONTH, month)
                }
                if (year != null) {
                    set(Calendar.YEAR, year)
                }
            }
        }

        private fun tokenize(value: String): List<String> {
            val sanitized = value.replace(INVALID_CHARACTER, "").trim()

            var tokens: List<String>

            if (sanitized.isEmpty()) {
                tokens = emptyList()
            } else if (sanitized.matches(THREE_OR_FOUR_DIGITS)) {
                tokens = listOf(
                  sanitized.substring(0, 2),
                  sanitized.substring(2)
                )
            } else {
                tokens = sanitized.split(SEPARATOR_CHAR)
            }

            tokens = tokens.filter { it.isNotEmpty() }
            tokens = tokens.subList(0, min(tokens.size, 2))

            return tokens
        }

        private fun parseMonth(str: String, autoInsert: Boolean): String {
            var sanitized = str.trim()

            if (sanitized.isEmpty()) {
                return sanitized
            }

            if (sanitized.matches(ZERO_OR_ONE)) {
                return sanitized
            }

            if (autoInsert && sanitized.matches(TWO_THROUGH_NINE)) {
                return "0$sanitized"
            }

            if (sanitized.length > 2) {
                sanitized = sanitized.substring(0, 2)
            }

            if (sanitized.matches(INVALID_MONTH)) {
                return sanitized.replaceRange(1, 2, "2")
            }

            if (sanitized == "00") {
                return "0"
            }

            if (!sanitized.matches(BEGINS_WITH_ZERO_OR_ONE)) {
                return "0" + sanitized.substring(0, 1)
            }

            return sanitized
        }

        private fun parseYear(str: String): String {
            val sanitized = str.trim()

            if (sanitized.isEmpty()) {
                return sanitized
            }

            if (sanitized.length > 2) {
                return sanitized.substring(0, 2)
            }

            return sanitized
        }
    }
}
