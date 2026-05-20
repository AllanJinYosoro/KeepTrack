package com.example.keeptrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.ExerciseType

@Composable
fun ManualInputDialog(
    exerciseType: ExerciseType,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var minutesText by remember { mutableStateOf("") }
    var secondsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "输入时长: ${exerciseType.name}") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it },
                    label = { Text("分钟") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secondsText,
                    onValueChange = { secondsText = it },
                    label = { Text("秒") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val mins = minutesText.toLongOrNull() ?: 0L
                    val secs = secondsText.toLongOrNull() ?: 0L
                    val totalMillis = (mins * 60 + secs) * 1000
                    if (totalMillis > 0) {
                        onSave(totalMillis)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
