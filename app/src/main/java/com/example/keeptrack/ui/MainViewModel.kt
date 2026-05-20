package com.example.keeptrack.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keeptrack.data.AppDatabase
import com.example.keeptrack.data.AppRepository
import com.example.keeptrack.data.ExerciseType
import com.example.keeptrack.data.prefs.SettingsManager
import com.example.keeptrack.service.TimerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val settingsManager = SettingsManager(application)
    
    val allExerciseTypes: StateFlow<List<ExerciseType>>
    val totalMonthlyWeightedMinutes: StateFlow<Double>
    val monthlyGoal: StateFlow<Double>

    private val _timerService = MutableStateFlow<TimerService?>(null)
    val timerSecondsElapsed: StateFlow<Long> = _timerService.flatMapLatest { it?.secondsElapsed ?: MutableStateFlow(0L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val isTimerRunning: StateFlow<Boolean> = _timerService.flatMapLatest { it?.isRunning ?: MutableStateFlow(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            _timerService.value = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _timerService.value = null
        }
    }

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

        // Bind to Service
        Intent(application, TimerService::class.java).also { intent ->
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun startTimer() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun pauseTimer() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun resumeTimer() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_RESUME
        }
        getApplication<Application>().startService(intent)
    }

    fun stopTimer() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unbindService(serviceConnection)
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
