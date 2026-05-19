package com.example.keeptrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.ExerciseType
import com.example.keeptrack.ui.components.ExerciseTimerDialog
import com.example.keeptrack.ui.components.ManualInputDialog
import com.example.keeptrack.ui.components.SettingsDialog

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val totalMinutes by viewModel.totalMonthlyWeightedMinutes.collectAsState()
    val exerciseTypes by viewModel.allExerciseTypes.collectAsState()
    val goal by viewModel.monthlyGoal.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var selectedTypeForTimer by remember { mutableStateOf<ExerciseType?>(null) }
    var selectedTypeForInput by remember { mutableStateOf<ExerciseType?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PowerFit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "月度目标完成度",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (goal > 0) (totalMinutes / goal).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${totalMinutes.toInt()} / ${goal.toInt()} 加权分钟",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exerciseTypes) { type ->
                    ExerciseTypeCard(
                        type = type,
                        onTimerClick = { selectedTypeForTimer = type },
                        onInputClick = { selectedTypeForInput = type }
                    )
                }
            }
        }

        // Dialogs
        if (showSettings) {
            SettingsDialog(
                currentGoal = goal,
                exerciseTypes = exerciseTypes,
                onDismiss = { showSettings = false },
                onSaveGoal = { viewModel.updateMonthlyGoal(it) },
                onUpdateType = { viewModel.updateExerciseType(it) },
                onAddType = { viewModel.addExerciseType(it) },
                onDeleteType = { viewModel.deleteExerciseType(it) }
            )
        }

        selectedTypeForTimer?.let { type ->
            ExerciseTimerDialog(
                exerciseType = type,
                onDismiss = { selectedTypeForTimer = null },
                onSave = { 
                    viewModel.addRecord(type, it)
                    selectedTypeForTimer = null
                }
            )
        }

        selectedTypeForInput?.let { type ->
            ManualInputDialog(
                exerciseType = type,
                onDismiss = { selectedTypeForInput = null },
                onSave = {
                    viewModel.addRecord(type, it)
                    selectedTypeForInput = null
                }
            )
        }
    }
}

@Composable
fun ExerciseTypeCard(
    type: ExerciseType,
    onTimerClick: () -> Unit,
    onInputClick: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = type.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (type.description.isNotEmpty()) {
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "权重: ${type.weight}x" + (if (type.monthlyCap != null) " | 上限: ${type.monthlyCap.toInt()} min" else ""),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInputClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("输入时长")
                }
                Button(
                    onClick = onTimerClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("计时开始")
                }
            }
        }
    }
}
