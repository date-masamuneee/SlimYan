@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.ui.food.FoodManagementSheet
import com.example.slimyan.ui.food.FoodViewModel

@Composable
fun MealScreen(
    vm: MealViewModel = hiltViewModel(),
    foodVm: FoodViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var addTargetSlot by remember { mutableStateOf<String?>(null) }
    var showFoodSheet by remember { mutableStateOf(false) }
    var subTab by remember { mutableStateOf(MealSubTab.Record) }

    if (showFoodSheet) {
        FoodManagementSheet(vm = foodVm, onDismiss = { showFoodSheet = false })
    }

    if (addTargetSlot != null) {
        FoodPickerDialog(
            slot = addTargetSlot!!,
            foods = state.foods,
            query = state.foodQuery,
            onQueryChange = { vm.setQuery(it) },
            onAdd = { food, grams -> vm.addEntry(food, addTargetSlot!!, grams) },
            onDismiss = { addTargetSlot = null; vm.setQuery("") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食事") },
                actions = {
                    IconButton(onClick = { showFoodSheet = true }) {
                        Icon(Icons.Filled.Restaurant, "マイ食品管理")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // サブタブ切替（記録 / 週間テンプレ）
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealSubTab.entries.forEach { tab ->
                    FilterChip(
                        selected = subTab == tab,
                        onClick = { subTab = tab },
                        label = { Text(tab.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (subTab) {
                MealSubTab.Record -> RecordContent(
                    state = state,
                    onAddClick = { addTargetSlot = it },
                    onToggleCheck = { vm.toggleCheck(it) },
                    onDelete = { vm.delete(it) }
                )
                MealSubTab.Template -> TemplateEditor()
                MealSubTab.Shopping -> ShoppingListContent()
            }
        }
    }
}

private enum class MealSubTab(val label: String) {
    Record("記録"), Template("週間テンプレ"), Shopping("買い物")
}

@Composable
private fun RecordContent(
    state: MealUiState,
    onAddClick: (String) -> Unit,
    onToggleCheck: (MealEntry) -> Unit,
    onDelete: (MealEntry) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // カロリーサマリ
        if (state.dailyTarget > 0) {
            val remaining = state.dailyTarget - state.checkedCalories
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "摂取済み ${state.checkedCalories.toInt()} / 目標 ${state.dailyTarget} kcal",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "残り ${remaining.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remaining < 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    LinearProgressIndicator(
                        progress = { (state.checkedCalories / state.dailyTarget).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // スロット別表示
        MEAL_SLOTS.forEach { (slot, label) ->
            val slotEntries = state.entries.filter { it.mealSlot == slot }
            MealSlotCard(
                label = label,
                entries = slotEntries,
                onAddClick = { onAddClick(slot) },
                onToggleCheck = onToggleCheck,
                onDelete = onDelete
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MealSlotCard(
    label: String,
    entries: List<MealEntry>,
    onAddClick: () -> Unit,
    onToggleCheck: (MealEntry) -> Unit,
    onDelete: (MealEntry) -> Unit,
) {
    val slotKcal = entries.sumOf { it.calories.toDouble() }.toInt()
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (slotKcal > 0) Text("$slotKcal kcal", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "追加", modifier = Modifier.size(20.dp))
                    }
                }
            }
            entries.forEach { entry ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = entry.isChecked,
                            onCheckedChange = { onToggleCheck(entry) }
                        )
                        Column {
                            Text(
                                entry.foodName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (entry.isChecked) MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${entry.grams.toInt()}g · ${entry.calories.toInt()} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "削除", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodPickerDialog(
    slot: String,
    foods: List<Food>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: (Food, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var grams by remember(selectedFood) {
        mutableStateOf(selectedFood?.servingGrams?.toString() ?: "100")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("食品を追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("食品名で検索") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    foods.forEach { food ->
                        val isSelected = selectedFood?.id == food.id
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedFood = food; grams = food.servingGrams.toString() }
                            )
                            Column(Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${food.calories.toInt()} kcal / 1食(${food.servingGrams.toInt()}g)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                if (selectedFood != null) {
                    val kcal = (selectedFood!!.calories * (grams.toFloatOrNull() ?: 0f) / selectedFood!!.servingGrams).toInt()
                    OutlinedTextField(
                        value = grams,
                        onValueChange = { grams = it },
                        label = { Text("グラム数 (→ $kcal kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val food = selectedFood ?: return@TextButton
                    val g = grams.toFloatOrNull() ?: return@TextButton
                    onAdd(food, g)
                    onDismiss()
                },
                enabled = selectedFood != null && grams.toFloatOrNull() != null
            ) { Text("追加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
