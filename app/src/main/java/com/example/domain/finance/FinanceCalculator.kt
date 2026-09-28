package com.example.domain.finance

import java.util.Locale

/**
 * Domain-level money and payment plan calculations.
 * Values are stored and calculated strictly using integer minor units (e.g. dirhams/cents/fils)
 * to prevent floating point inaccuracies.
 */
object FinanceCalculator {

    /**
     * Converts a major unit currency (e.g. 400.00 QAR) to integer minor units (e.g. 40000).
     */
    fun toMinorUnits(amount: Double): Long {
        return Math.round(amount * 100.0)
    }

    /**
     * Converts integer minor units (e.g. 40000) back to standard double for display or serialization.
     */
    fun fromMinorUnits(minorUnits: Long): Double {
        return minorUnits / 100.0
    }

    /**
     * Formats integer minor units with currency code safely.
     */
    fun formatMoney(minorUnits: Long, currency: String = "QAR"): String {
        val whole = minorUnits / 100
        val frac = Math.abs(minorUnits % 100)
        return if (frac == 0L) {
            "$currency $whole"
        } else {
            String.format(Locale.US, "$currency %d.%02d", whole, frac)
        }
    }

    /**
     * Calculates the remaining balance due given total expected and total paid (in minor units).
     */
    fun calculateStudentBalance(totalExpectedMinor: Long, totalPaidMinor: Long): Long {
        return maxOf(0L, totalExpectedMinor - totalPaidMinor)
    }

    /**
     * Calculates total expected billing amount for a student based on plan type and activity.
     */
    fun calculateExpectedAmount(
        paymentType: String,
        defaultPriceMinor: Long,
        completedBillableLessons: Int,
        monthsActive: Int = 1,
        packagePriceMinor: Long = 0L,
        customAmountMinor: Long = 0L
    ): Long {
        return when (paymentType) {
            "PACKAGE" -> packagePriceMinor
            "MONTHLY" -> defaultPriceMinor * maxOf(1, monthsActive)
            "CUSTOM" -> if (customAmountMinor > 0L) customAmountMinor else defaultPriceMinor
            else -> defaultPriceMinor * completedBillableLessons // PER_LESSON
        }
    }

    /**
     * Calculates monthly fee remaining balance.
     */
    fun calculateMonthlyDue(monthlyFeeMinor: Long, paymentsThisMonthMinor: Long): Long {
        return maxOf(0L, monthlyFeeMinor - paymentsThisMonthMinor)
    }

    /**
     * Calculates package lessons remaining safely.
     */
    fun calculatePackageRemaining(totalLessons: Int, usedLessons: Int): Int {
        return maxOf(0, totalLessons - usedLessons)
    }

    /**
     * Sums list of payments in minor units.
     */
    fun calculatePaymentsReceived(paymentsMinor: List<Long>): Long {
        return paymentsMinor.sum()
    }
}
