package com.example.keeptrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_records")
data class ExerciseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val typeId: Int,
    val timestamp: Long, // Start time of the exercise
    val durationMillis: Long,
    val weightedMinutes: Double
)
