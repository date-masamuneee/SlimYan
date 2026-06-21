@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.food

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.remote.NutritionEstimate

@Composable
fun FoodManagementSheet(
    vm: FoodViewModel,
    onDismiss: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val estimateState by vm.estimate.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<Food?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    if (showAddForm || editTarget != null) {
        FoodEditDialog(
            initial = editTarget,
            estimateState = estimateState,
            onEstimateRequest = { vm.requestEstimate(it) },
            onConfirm = { name, kcal, p, f, c, g, fav ->
                if (editTarget != null) {
                    vm.update(editTarget!!.copy(
                        name = name, calories = kcal,
                        protein = p, fat = f,
                        carb = c, servingGrams = g, isFavorite = fav,
                    ))
                } else {
                    vm.add(name, kcal, p, f, c, g, fav)
                }
                editTarget = null
                showAddForm = false
                vm.clearEstimate()
            },
            onDismiss = {
                editTarget = null
                showAddForm = false
                vm.clearEstimate()
            }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ヘッダー
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("マイ食品", style = MaterialTheme.typography.titleLarge)
                FilledTonalButton(onClick = { showAddForm = true }) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("追加")
                }
            }

            // 検索
            OutlinedTextField(
                value = state.query,
                onValueChange = { vm.setQuery(it) },
                placeholder = { Text("食品名で検索") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 食品リスト
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                items(state.foods, key = { it.id }) { food ->
                    FoodRow(
                        food = food,
                        onFavorite = { vm.toggleFavorite(food) },
                        onEdit = { editTarget = food },
                        onDelete = { vm.delete(food) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodRow(
    food: Food,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (food.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "お気に入り",
                    tint = if (food.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${food.calories.toInt()} kcal · P${food.protein}g F${food.fat}g C${food.carb}g / 1食(${food.servingGrams.toInt()}g)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, "編集", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, "削除", modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FoodEditDialog(
    initial: Food?,
    estimateState: EstimateState,
    onEstimateRequest: (String) -> Unit,
    onConfirm: (String, Float, Float, Float, Float, Float, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var kcal by remember(initial) { mutableStateOf(initial?.calories?.toString() ?: "") }
    var protein by remember(initial) { mutableStateOf(initial?.protein?.toString() ?: "") }
    var fat by remember(initial) { mutableStateOf(initial?.fat?.toString() ?: "") }
    var carb by remember(initial) { mutableStateOf(initial?.carb?.toString() ?: "") }
    var grams by remember(initial) { mutableStateOf(initial?.servingGrams?.toString() ?: "100") }
    var fav by remember(initial) { mutableStateOf(initial?.isFavorite ?: false) }

    // 推定結果が来たら自動入力
    LaunchedEffect(estimateState) {
        if (estimateState is EstimateState.Done) {
            val e = estimateState.estimate
            kcal = e.calories.toInt().toString()
            protein = "%.1f".format(e.protein)
            fat = "%.1f".format(e.fat)
            carb = "%.1f".format(e.carb)
            grams = e.servingGrams.toInt().toString()
        }
    }

    val isValid = name.isNotBlank()
        && kcal.toFloatOrNull() != null
        && grams.toFloatOrNull() != null
    val isEstimating = estimateState is EstimateState.Loading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "食品を追加" else "食品を編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("食品名 *") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = { onEstimateRequest(name) },
                        enabled = name.isNotBlank() && !isEstimating,
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (isEstimating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("AI推定", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (estimateState is EstimateState.Error) {
                    Text(
                        "推定失敗: ${estimateState.msg}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                DecimalField("カロリー (kcal/1食) *", kcal) { kcal = it }
                Text(
                    "値はすべて一食分。PFC は省略可（空欄 = 0g として保存）",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DecimalField("P (g)", protein, Modifier.weight(1f)) { protein = it }
                    DecimalField("F (g)", fat, Modifier.weight(1f)) { fat = it }
                    DecimalField("C (g)", carb, Modifier.weight(1f)) { carb = it }
                }
                DecimalField("一食の量 (g) *", grams) { grams = it }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fav, onCheckedChange = { fav = it })
                    Text("お気に入り", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        kcal.toFloat(),
                        protein.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f,
                        carb.toFloatOrNull() ?: 0f,
                        grams.toFloat(),
                        fav,
                    )
                },
                enabled = isValid
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

@Composable
private fun DecimalField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true, modifier = modifier
    )
}
