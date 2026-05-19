package com.example.keeptrack.data

import com.example.keeptrack.logic.WeightCalculator
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AppRepository(private val exerciseDao: ExerciseDao) {
    val allExerciseTypes: Flow<List<ExerciseType>> = exerciseDao.getAllExerciseTypes()
    val allRecords: Flow<List<ExerciseRecord>> = exerciseDao.getAllRecords()

    fun getTotalMonthlyWeightedMinutes(): Flow<Double?> {
        val startOfMonth = getStartOfMonth()
        return exerciseDao.getTotalMonthlyWeightedMinutes(startOfMonth)
    }

    suspend fun insertRecord(type: ExerciseType, durationMillis: Long, timestamp: Long) {
        val startOfMonth = getStartOfMonth()
        val currentMonthly = exerciseDao.getMonthlyWeightedMinutesForType(type.id, startOfMonth) ?: 0.0
        
        val weightedMinutes = WeightCalculator.calculateWeightedMinutes(
            durationMillis = durationMillis,
            weight = type.weight,
            currentMonthlyWeightedMinutes = currentMonthly,
            monthlyCap = type.monthlyCap
        )
        
        val record = ExerciseRecord(
            typeId = type.id,
            timestamp = timestamp,
            durationMillis = durationMillis,
            weightedMinutes = weightedMinutes
        )
        exerciseDao.insertRecord(record)
    }

    suspend fun insertExerciseType(exerciseType: ExerciseType) {
        exerciseDao.insertExerciseType(exerciseType)
    }

    suspend fun updateExerciseType(exerciseType: ExerciseType) {
        exerciseDao.updateExerciseType(exerciseType)
    }

    suspend fun deleteExerciseType(exerciseType: ExerciseType) {
        exerciseDao.deleteExerciseType(exerciseType)
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
