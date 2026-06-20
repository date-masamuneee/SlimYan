@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.ui.meal.MEAL_SLOTS
import java.time.LocalDate

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Slim-yan") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // カロリーカード
            CalorieCard(
                target = state.dailyCalorieTarget,
                checked = state.checkedCalories,
            )

            // 食事サマリ
            MealSummaryCard(entries = state.todayEntries)

            // 筋トレ
            WorkoutCard(hasSets = state.todaySets.isNotEmpty())

            // 体重トレンド
            if (state.recentWeights.isNotEmpty()) {
                WeightTrendCard(weights = state.recentWeights)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalorieCard(target: Int, checked: Float) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("今日のカロリー", style = MaterialTheme.typography.titleMedium)
            if (target <= 0) {
                Text("設定画面で目標を設定してください", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            } else {
                val remaining = target - checked
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${checked.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("/ $target kcal", style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 2.dp))
                }
                LinearProgressIndicator(
                    progress = { (checked / target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "残り ${remaining.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun MealSummaryCard(entries: List<com.example.slimyan.data.entity.MealEntry>) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("今日の食事", style = MaterialTheme.typography.titleMedium)
            if (entries.isEmpty()) {
                Text("食事タブからプランを追加してください", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            } else {
                MEAL_SLOTS.forEach { (slot, label) ->
                    val slotEntries = entries.filter { it.mealSlot == slot }
                    val checked = slotEntries.count { it.isChecked }
                    val total = slotEntries.size
                    if (total > 0) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            Text("$checked / $total 食べた", style = MaterialTheme.typography.bodySmall,
                                color = if (checked == total) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(hasSets: Boolean) {
    Card {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text("今日の筋トレ", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (hasSets) "記録あり ✓" else "まだ記録なし",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasSets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun WeightTrendCard(weights: List<BodyWeight>) {
    val sorted = weights.sortedBy { it.dateEpochDay }
    val latest = sorted.lastOrNull()
    val prev = sorted.dropLast(1).lastOrNull()

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("体重トレンド（直近7日）", style = MaterialTheme.typography.titleMedium)
            if (latest != null) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${latest.weightKg} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (prev != null) {
                        val diff = latest.weightKg - prev.weightKg
                        val sign = if (diff > 0) "+" else ""
                        Text(
                            "$sign${"%.1f".format(diff)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (diff > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                Text(
                    "最終記録: ${
                        runCatching {
                            LocalDate.ofEpochDay(latest.dateEpochDay).toString()
                        }.getOrDefault("-")
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
