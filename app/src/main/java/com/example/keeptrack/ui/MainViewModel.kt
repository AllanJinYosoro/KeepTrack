package com.example.keeptrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keeptrack.data.AppDatabase
import com.example.keeptrack.data.AppRepository
import com.example.keeptrack.data.ExerciseType
import com.example.keeptrack.data.prefs.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val settingsManager = SettingsManager(application)
    
    val allExerciseTypes: StateFlow<List<ExerciseType>>
    val totalMonthlyWeightedMinutes: StateFlow<Double>
    val monthlyGoal: StateFlow<Double>

    init {
        val exerciseDao = AppDatabase.getDatabase(application).exerciseDao()
        repository = AppRepository(exerciseDao)
        
        allExerciseTypes = repository.allExerciseTypes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        totalMonthlyWeightedMinutes = repository.getTotalMonthlyWeightedMinutes().map { it ?: 0.0 }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
        monthlyGoal = settingsManager.monthlyGoal.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 700.0
        )
    }

    fun addRecord(type: ExerciseType, durationMillis: Long) {
        viewModelScope.launch {
            repository.insertRecord(type, durationMillis, System.currentTimeMillis())
        }
    }

    fun updateMonthlyGoal(newGoal: Double) {
        viewModelScope.launch {
            settingsManager.saveMonthlyGoal(newGoal)
        }
    }

    fun updateExerciseType(type: ExerciseType) {
        viewModelScope.launch {
            repository.updateExerciseType(type)
        }
    }

    fun addExerciseType(type: ExerciseType) {
        viewModelScope.launch {
            repository.insertExerciseType(type)
        }
    }

    fun deleteExerciseType(type: ExerciseType) {
        viewModelScope.launch {
            repository.deleteExerciseType(type)
        }
    }
}
