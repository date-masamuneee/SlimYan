@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.data.remote.RecoverySuggestion
import com.example.slimyan.ui.meal.MEAL_SLOTS
import java.time.LocalDate

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val recovery by vm.recovery.collectAsStateWithLifecycle()

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

            // リカバリ提案
            RecoverySection(
                recovery = recovery,
                canRequest = state.dailyCalorieTarget > 0,
                onRequest = { vm.requestRecovery() },
                onDismiss = { vm.dismissRecovery() },
                onNavigateToSettings = onNavigateToSettings,
            )

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
private fun RecoverySection(
    recovery: RecoveryState,
    canRequest: Boolean,
    onRequest: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    when (recovery) {
        is RecoveryState.Idle -> {
            if (canRequest) {
                OutlinedButton(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AIリカバリ提案を取得")
                }
            } else {
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Settings, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("目標を設定してAI提案を有効化")
                }
            }
        }
        is RecoveryState.Loading -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("AIが提案を生成中...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        is RecoveryState.Success -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AIリカバリ提案", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onDismiss) { Text("閉じる") }
                    }
                    Text(
                        recovery.suggestion.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    HorizontalDivider()
                    Text("今日の残り食事プラン", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Text(recovery.suggestion.todayPlan, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text("明日のプラン修正", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Text(recovery.suggestion.tomorrowPlan, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        is RecoveryState.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("取得エラー", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onDismiss) { Text("閉じる") }
                    }
                    Text(
                        recovery.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
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
