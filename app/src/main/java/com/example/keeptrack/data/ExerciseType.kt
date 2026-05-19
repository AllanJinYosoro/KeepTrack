package com.example.keeptrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_types")
data class ExerciseType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val weight: Double,
    val monthlyCap: Double? = null, // null means no cap
    val description: String = ""
)
