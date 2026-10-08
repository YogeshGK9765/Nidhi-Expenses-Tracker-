package com.example.domain

import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {

    /**
     * Formats integer minor units (paise) to Indian Rupee string without floating point arithmetic.
     * E.g.:
     * 8050L -> "₹80.50"
     * 8000L -> "₹80"
     * 825000L -> "₹8,250"
     * 10000000L -> "₹1,00,000"
     */
    fun formatPaise(paise: Long, forceDecimals: Boolean = false, includePrefixSign: Boolean = false): String {
        val isNegative = paise < 0
        val absPaise = if (isNegative) -paise else paise
        val rupees = absPaise / 100
        val remainingPaise = (absPaise % 100).toInt()

        val formattedRupees = formatIndianNumber(rupees)

        val amountStr = if (remainingPaise > 0 || forceDecimals) {
            val paiseStr = remainingPaise.toString().padStart(2, '0')
            "₹$formattedRupees.$paiseStr"
        } else {
            "₹$formattedRupees"
        }

        return when {
            isNegative && includePrefixSign -> "- $amountStr"
            isNegative -> "-$amountStr"
            else -> amountStr
        }
    }

    /**
     * Formats integer number into Indian comma grouping:
     * Last 3 digits grouped, then pairs of 2 digits.
     * E.g. 1234567 -> "12,34,567"
     */
    private fun formatIndianNumber(number: Long): String {
        val s = number.toString()
        if (s.length <= 3) return s

        val lastThree = s.substring(s.length - 3)
        var remaining = s.substring(0, s.length - 3)

        val sb = StringBuilder()
        while (remaining.length > 2) {
            val chunk = remaining.substring(remaining.length - 2)
            sb.insert(0, ",$chunk")
            remaining = remaining.substring(0, remaining.length - 2)
        }
        if (remaining.isNotEmpty()) {
            sb.insert(0, remaining)
        }
        sb.append(",").append(lastThree)
        return sb.toString()
    }

    /**
     * Parses user rupee text input into integer paise (minor units).
     * Strictly avoids floating-point inaccuracies.
     * E.g.:
     * "80" -> 8000L
     * "80.5" -> 8050L
     * "80.50" -> 8050L
     * "8,250" -> 825000L
     * "80.05" -> 8005L
     */
    fun parseInputToPaise(input: String): Long {
        val clean = input.replace("₹", "").replace(",", "").trim()
        if (clean.isEmpty()) return 0L

        if (!clean.contains(".")) {
            val r = clean.toLongOrNull() ?: 0L
            return r * 100L
        }

        val parts = clean.split(".")
        val rupeePart = parts[0].toLongOrNull() ?: 0L
        val paisePartString = parts.getOrNull(1) ?: ""

        val normalizedPaise = when {
            paisePartString.isEmpty() -> 0L
            paisePartString.length == 1 -> (paisePartString.toLongOrNull() ?: 0L) * 10L
            paisePartString.length == 2 -> paisePartString.toLongOrNull() ?: 0L
            else -> paisePartString.substring(0, 2).toLongOrNull() ?: 0L
        }

        return (rupeePart * 100L) + normalizedPaise
    }
}
