@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.Exercise

@Composable
fun WorkoutProgramSection(vm: WorkoutProgramViewModel = hiltViewModel()) {
    val rows by vm.rows.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        AddProgramDialog(
            exercises = exercises,
            onConfirm = { exId, sets, ceiling -> vm.addToProgram(exId, sets, ceiling); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("今日のプログラム", style = MaterialTheme.typography.titleMedium)
        FilledTonalButton(onClick = { showAdd = true }) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("種目追加")
        }
    }

    if (rows.isEmpty()) {
        Text("プログラムに種目を追加すると、ダブルプログレッションの重量UP提案が出るよ",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }

    rows.forEach { row ->
        Card {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(row.exercise?.name ?: "（不明な種目）", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "目標 ${row.item.targetSets}セット × ${row.item.repCeiling}回まで",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    val last = row.lastSession.firstOrNull()
                    if (last != null) {
                        Text("前回: ${last.weight}kg × ${row.lastSession.joinToString("/") { it.reps.toString() }}回",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
                if (row.shouldIncrease) {
                    AssistChip(
                        onClick = {},
                        label = { Text("重量UP") },
                        leadingIcon = { Icon(Icons.Filled.TrendingUp, null, Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                IconButton(onClick = { vm.removeFromProgram(row.item) }) {
                    Icon(Icons.Filled.Delete, "削除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddProgramDialog(
    exercises: List<Exercise>,
    onConfirm: (Long, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var sets by remember { mutableStateOf("3") }
    var ceiling by remember { mutableStateOf("12") }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = exercises.firstOrNull { it.id == selectedId }?.name ?: "種目を選択"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("プログラムに種目を追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedName, onValueChange = {}, readOnly = true,
                        label = { Text("種目") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        exercises.forEach { ex ->
                            DropdownMenuItem(text = { Text(ex.name) },
                                onClick = { selectedId = ex.id; expanded = false })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets, onValueChange = { sets = it },
                        label = { Text("セット数") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ceiling, onValueChange = { ceiling = it },
                        label = { Text("上限レップ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = selectedId ?: return@TextButton
                    val s = sets.toIntOrNull() ?: return@TextButton
                    val c = ceiling.toIntOrNull() ?: return@TextButton
                    onConfirm(id, s, c)
                },
                enabled = selectedId != null && sets.toIntOrNull() != null && ceiling.toIntOrNull() != null
            ) { Text("追加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
