package com.example.keeptrack.logic

import kotlin.math.min

object WeightCalculator {
    /**
     * Calculates the weighted minutes for a new exercise record.
     * 
     * @param durationMillis The duration of the exercise in milliseconds.
     * @param weight The weight assigned to the exercise type.
     * @param currentMonthlyWeightedMinutes The total weighted minutes already accumulated for this type in the current month.
     * @param monthlyCap The maximum weighted minutes allowed for this type per month.
     * @return The weighted minutes to be recorded, potentially capped by the monthly limit.
     */
    fun calculateWeightedMinutes(
        durationMillis: Long,
        weight: Double,
        currentMonthlyWeightedMinutes: Double,
        monthlyCap: Double?
    ): Double {
        val rawWeightedMinutes = (durationMillis / 60000.0) * weight
        
        if (monthlyCap == null) {
            return rawWeightedMinutes
        }
        
        val remainingCap = monthlyCap - currentMonthlyWeightedMinutes
        return if (remainingCap <= 0) {
            0.0
        } else {
            min(rawWeightedMinutes, remainingCap)
        }
    }
}
