package com.example.keeptrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_types")
    fun getAllExerciseTypes(): Flow<List<ExerciseType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseType(exerciseType: ExerciseType)

    @Update
    suspend fun updateExerciseType(exerciseType: ExerciseType)

    @Delete
    suspend fun deleteExerciseType(exerciseType: ExerciseType)

    @Query("SELECT * FROM exercise_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ExerciseRecord>>

    @Insert
    suspend fun insertRecord(record: ExerciseRecord)

    @Query("SELECT SUM(weightedMinutes) FROM exercise_records WHERE typeId = :typeId AND timestamp >= :startOfMonth")
    suspend fun getMonthlyWeightedMinutesForType(typeId: Int, startOfMonth: Long): Double?

    @Query("SELECT SUM(weightedMinutes) FROM exercise_records WHERE timestamp >= :startOfMonth")
    fun getTotalMonthlyWeightedMinutes(startOfMonth: Long): Flow<Double?>
}
