package com.example.slimyan.ui.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.NutritionMath
import com.example.slimyan.data.TemplateSummary
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem

private val DAY_LABELS = listOf("月", "火", "水", "木", "金", "土", "日")

@Composable
fun TemplateEditor(vm: MealTemplateViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val expandEvent by vm.expandEvent.collectAsStateWithLifecycle()
    var addSlot by remember { mutableStateOf<String?>(null) }

    if (addSlot != null) {
        TemplateAddDialog(
            foods = state.allFoods,
            onAdd = { food, grams -> vm.addItem(addSlot!!, food.id, grams) },
            onDismiss = { addSlot = null }
        )
    }

    // 展開確認ダイアログ
    if (expandEvent is ExpandEvent.NeedConfirm) {
        AlertDialog(
            onDismissRequest = { vm.clearExpandEvent() },
            title = { Text("すでに今日の記録があります") },
            text = { Text("今日の記録に、テンプレの品目を追加で展開しますか？") },
            confirmButton = {
                TextButton(onClick = { vm.clearExpandEvent(); vm.confirmExpandToday() }) { Text("追加で展開") }
            },
            dismissButton = { TextButton(onClick = { vm.clearExpandEvent() }) { Text("キャンセル") } }
        )
    }
    // 完了/空テンプレの一時メッセージ
    val expandMessage = when (val e = expandEvent) {
        is ExpandEvent.Done -> "${e.count}品目を今日の記録に展開した"
        ExpandEvent.EmptyTemplate -> "今日の曜日のテンプレが空だよ"
        else -> null
    }
    LaunchedEffect(expandEvent) {
        if (expandEvent is ExpandEvent.Done || expandEvent is ExpandEvent.EmptyTemplate) {
            kotlinx.coroutines.delay(2500)
            vm.clearExpandEvent()
        }
    }

    val todayLabel = DAY_LABELS[java.time.LocalDate.now().dayOfWeek.value - 1]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 当日展開ボタン
        Button(
            onClick = { vm.requestExpandToday() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Today, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("今日（$todayLabel）の分を記録に展開")
        }
        if (expandMessage != null) {
            Text(expandMessage, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary)
        }

        // 曜日セレクタ
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..7).forEach { day ->
                FilterChip(
                    selected = state.selectedDay == day,
                    onClick = { vm.selectDay(day) },
                    label = { Text(DAY_LABELS[day - 1]) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 曜日合計・週合計
        TotalsCard(state.dayTotals, state.weekTotals, state.dailyTarget)

        // slot別
        MEAL_SLOTS.forEach { (slot, label) ->
            val items = state.dayItems.filter { it.mealSlot == slot }
            TemplateSlotCard(
                label = label,
                items = items,
                foodsById = state.foodsById,
                onAdd = { addSlot = slot },
                onDelete = { vm.deleteItem(it) }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TotalsCard(
    day: TemplateSummary.Totals,
    week: TemplateSummary.Totals,
    target: Int,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "この曜日: ${day.kcal.toInt()} kcal ・ P${day.protein.toInt()} F${day.fat.toInt()} C${day.carb.toInt()}",
                style = MaterialTheme.typography.titleSmall
            )
            if (target > 0) {
                val diff = day.kcal.toInt() - target
                val sign = if (diff > 0) "+" else ""
                Text(
                    "目標 $target kcal との差: $sign$diff kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (diff > 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                "週合計: ${week.kcal.toInt()} kcal ・ P${week.protein.toInt()}g（週平均 ${(week.kcal / 7).toInt()} kcal/日）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun TemplateSlotCard(
    label: String,
    items: List<MealTemplateItem>,
    foodsById: Map<Long, Food>,
    onAdd: () -> Unit,
    onDelete: (MealTemplateItem) -> Unit,
) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Add, "追加", modifier = Modifier.size(20.dp))
                }
            }
            if (items.isEmpty()) {
                Text("未設定", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            items.forEach { item ->
                val food = foodsById[item.foodId]
                val kcal = food?.let { NutritionMath.forGrams(it, item.grams).kcal.toInt() } ?: 0
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(food?.name ?: "（削除された食品）", style = MaterialTheme.typography.bodyMedium)
                        Text("${item.grams.toInt()}g · $kcal kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, "削除", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateAddDialog(
    foods: List<Food>,
    onAdd: (Food, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var grams by remember(selectedFood) { mutableStateOf(selectedFood?.servingGrams?.toString() ?: "100") }

    val filtered = remember(query, foods) {
        if (query.isBlank()) foods else foods.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("テンプレに品目を追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("食品名で検索") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    filtered.forEach { food ->
                        val isSelected = selectedFood?.id == food.id
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedFood = food; grams = food.servingGrams.toString() }
                            )
                            Column(Modifier.weight(1f)) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${food.calories.toInt()} kcal / 1食(${food.servingGrams.toInt()}g)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                            if (isSelected) Icon(Icons.Filled.Check, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (selectedFood != null) {
                    val kcal = ((selectedFood!!.calories * (grams.toFloatOrNull() ?: 0f)) / selectedFood!!.servingGrams).toInt()
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
