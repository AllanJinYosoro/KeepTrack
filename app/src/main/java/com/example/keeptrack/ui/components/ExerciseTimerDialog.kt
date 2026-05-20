package com.example.keeptrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.ExerciseType
import com.example.keeptrack.ui.MainViewModel

@Composable
fun ExerciseTimerDialog(
    exerciseType: ExerciseType,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val secondsElapsed by viewModel.timerSecondsElapsed.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startTimer()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "计时器: ${exerciseType.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(secondsElapsed),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        if (isRunning) viewModel.pauseTimer() else viewModel.resumeTimer()
                    }) {
                        Text(if (isRunning) "暂停" else "开始")
                    }
                    Button(onClick = { viewModel.stopTimer(); viewModel.startTimer() }) {
                        Text("重置")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                viewModel.addRecord(exerciseType, secondsElapsed * 1000)
                viewModel.stopTimer()
                onDismiss()
            }) {
                Text("完成并记录")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.stopTimer()
                onDismiss()
            }) {
                Text("取消")
            }
        }
    )
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
