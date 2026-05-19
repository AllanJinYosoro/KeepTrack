package com.example.keeptrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.ExerciseType
import kotlinx.coroutines.delay

@Composable
fun ExerciseTimerDialog(
    exerciseType: ExerciseType,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            secondsElapsed++
        }
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
                    Button(onClick = { isRunning = !isRunning }) {
                        Text(if (isRunning) "暂停" else "开始")
                    }
                    Button(onClick = { secondsElapsed = 0 }) {
                        Text("重置")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(secondsElapsed * 1000) }) {
                Text("完成并记录")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
