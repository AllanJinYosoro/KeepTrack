package com.example.keeptrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.ExerciseType

@Composable
fun SettingsDialog(
    currentGoal: Double,
    exerciseTypes: List<ExerciseType>,
    onDismiss: () -> Unit,
    onSaveGoal: (Double) -> Unit,
    onUpdateType: (ExerciseType) -> Unit,
    onAddType: (ExerciseType) -> Unit,
    onDeleteType: (ExerciseType) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }
    var showAddDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("系统配置") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("月度总目标 (加权分钟)", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("运动类型配置", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { showAddDialog = true }) {
                            Text("新增")
                        }
                    }
                }

                items(exerciseTypes) { type ->
                    TypeEditCard(
                        type = type,
                        onUpdate = onUpdateType,
                        onDelete = { onDeleteType(type) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newGoal = goalText.toDoubleOrNull() ?: currentGoal
                onSaveGoal(newGoal)
                onDismiss()
            }) {
                Text("完成")
            }
        }
    )

    if (showAddDialog) {
        AddEditTypeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { 
                onAddType(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TypeEditCard(
    type: ExerciseType,
    onUpdate: (ExerciseType) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(type.name, fontWeight = FontWeight.Bold)
                Text("权重: ${type.weight} | 上限: ${type.monthlyCap ?: "无"}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = { showEditDialog = true }) { Text("编辑") }
                TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (showEditDialog) {
        AddEditTypeDialog(
            initialType = type,
            onDismiss = { showEditDialog = false },
            onSave = {
                onUpdate(it)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun AddEditTypeDialog(
    initialType: ExerciseType? = null,
    onDismiss: () -> Unit,
    onSave: (ExerciseType) -> Unit
) {
    var name by remember { mutableStateOf(initialType?.name ?: "") }
    var weight by remember { mutableStateOf(initialType?.weight?.toString() ?: "1.0") }
    var cap by remember { mutableStateOf(initialType?.monthlyCap?.toString() ?: "") }
    var desc by remember { mutableStateOf(initialType?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialType == null) "新增类型" else "编辑类型") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") })
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("权重") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = cap, onValueChange = { cap = it }, label = { Text("月上限 (可选)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("描述") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(ExerciseType(
                    id = initialType?.id ?: 0,
                    name = name,
                    weight = weight.toDoubleOrNull() ?: 1.0,
                    monthlyCap = cap.toDoubleOrNull(),
                    description = desc
                ))
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
