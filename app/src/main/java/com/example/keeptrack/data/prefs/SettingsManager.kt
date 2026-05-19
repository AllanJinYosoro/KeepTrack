package com.example.keeptrack.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val MONTHLY_GOAL_KEY = doublePreferencesKey("monthly_goal")
    }

    val monthlyGoal: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_GOAL_KEY] ?: 700.0
    }

    suspend fun saveMonthlyGoal(goal: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_GOAL_KEY] = goal
        }
    }
}
