@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.Exercise
import com.example.slimyan.data.entity.SetEntry

@Composable
fun WorkoutScreen(vm: WorkoutViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    if (state.showAddExercise) {
        AddExerciseDialog(
            name = state.newExerciseName,
            group = state.newMuscleGroup,
            onNameChange = { vm.setNewExerciseName(it) },
            onGroupChange = { vm.setNewMuscleGroup(it) },
            onConfirm = { vm.addExercise() },
            onDismiss = { vm.showAddExercise(false) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("筋トレ記録") },
                actions = {
                    IconButton(onClick = { vm.showAddExercise(true) }) {
                        Icon(Icons.Filled.Add, "種目追加")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 固定プログラム＋ダブルプログレッション
            WorkoutProgramSection()

            HorizontalDivider()

            // 種目選択
            ExerciseSelector(
                exercises = state.exercises,
                selectedId = state.selectedExerciseId,
                onSelect = { vm.selectExercise(it) }
            )

            // 前回値の参照
            val selected = state.exercises.firstOrNull { it.id == state.selectedExerciseId }
            if (selected != null) {
                val prevForExercise = state.prevSets.filter { it.exerciseId == selected.id }
                if (prevForExercise.isNotEmpty()) {
                    val last = prevForExercise.first()
                    Text(
                        "前回: ${last.weight}kg × ${last.reps}rep",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // セット追加フォーム
                Card {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("セット追加（${selected.name}）", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.weightInput,
                                onValueChange = { vm.setWeight(it) },
                                label = { Text("重量 (kg)") },
                                placeholder = { Text(prevForExercise.firstOrNull()?.weight?.toString() ?: "0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.repsInput,
                                onValueChange = { vm.setReps(it) },
                                label = { Text("レップ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { vm.addSet() },
                                enabled = state.weightInput.toFloatOrNull() != null
                            ) { Text("追加") }
                        }
                    }
                }
            }

            // 今日の記録（種目別）
            val todayByExercise = state.todaySets.groupBy { it.exerciseId }
            if (todayByExercise.isNotEmpty()) {
                Text("今日の記録", style = MaterialTheme.typography.titleMedium)
                todayByExercise.forEach { (exId, sets) ->
                    val exName = state.exercises.firstOrNull { it.id == exId }?.name ?: "不明"
                    TodayExerciseCard(exName, sets, onDelete = { vm.deleteSet(it) })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExerciseSelector(
    exercises: List<Exercise>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = exercises.firstOrNull { it.id == selectedId }?.name ?: "種目を選択"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("種目") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            val grouped = exercises.groupBy { it.muscleGroup }
            grouped.forEach { (group, exList) ->
                DropdownMenuItem(
                    text = { Text(group, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline) },
                    onClick = {},
                    enabled = false
                )
                exList.forEach { ex ->
                    DropdownMenuItem(
                        text = { Text("　${ex.name}") },
                        onClick = { onSelect(ex.id); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayExerciseCard(name: String, sets: List<SetEntry>, onDelete: (SetEntry) -> Unit) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.titleSmall)
            sets.forEachIndexed { i, set ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${i + 1}セット: ${set.weight}kg × ${set.reps}rep",
                        style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onDelete(set) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(
    name: String,
    group: String,
    onNameChange: (String) -> Unit,
    onGroupChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("種目を追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("種目名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = group,
                    onValueChange = onGroupChange,
                    label = { Text("部位（例: 胸, 背中, 脚）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) { Text("追加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
