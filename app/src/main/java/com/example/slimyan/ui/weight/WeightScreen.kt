@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.entity.BodyWeight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("M/d")

@Composable
fun WeightScreen(vm: WeightViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("体重記録") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // 今日の体重入力
            Card {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("今日の体重", style = MaterialTheme.typography.titleMedium)
                    if (state.todayKg != null) {
                        Text("記録済み: ${state.todayKg} kg", color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.inputKg,
                            onValueChange = { vm.setInput(it) },
                            label = { Text("体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { vm.save() }) { Text("記録") }
                    }
                    if (state.goalWeightKg > 0f && state.todayKg != null) {
                        val diff = state.todayKg!! - state.goalWeightKg
                        val diffText = if (diff > 0) "目標まであと %.1f kg".format(diff)
                        else "目標体重達成！"
                        Text(diffText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 期間切替
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeightPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = state.period == p,
                        onClick = { vm.setPeriod(p) },
                        label = { Text(if (p == WeightPeriod.WEEK) "週" else "月") }
                    )
                }
            }

            // グラフ
            if (state.entries.size >= 2) {
                WeightGraph(state.entries, state.goalWeightKg)
            } else {
                Card {
                    Box(
                        Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("データが2件以上あるとグラフが表示されます", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 一覧
            state.entries.sortedByDescending { it.dateEpochDay }.forEach { entry ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = runCatching {
                        LocalDate.ofEpochDay(entry.dateEpochDay).format(DATE_FMT)
                    }.getOrDefault("-")
                    Text(label)
                    Text("${entry.weightKg} kg")
                    TextButton(onClick = { vm.delete(entry) }) { Text("削除") }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeightGraph(entries: List<BodyWeight>, goalKg: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val goal = MaterialTheme.colorScheme.error

    Card {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp)
        ) {
            val weights = entries.map { it.weightKg }
            val rawMin = weights.min()
            val rawMax = weights.max()
            val candidates = if (goalKg > 0f) listOf(rawMin, rawMax, goalKg) else listOf(rawMin, rawMax)
            val minW = candidates.min() - 0.5f
            val maxW = candidates.max() + 0.5f
            val range = (maxW - minW).coerceAtLeast(1f)

            fun yFor(w: Float) = size.height - (size.height * (w - minW) / range)
            fun xFor(i: Int) = if (entries.size == 1) size.width / 2 else size.width * i / (entries.size - 1)

            // 目標体重の水平線
            if (goalKg > 0f) {
                val gy = yFor(goalKg)
                drawLine(goal, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1.dp.toPx())
            }

            // 体重折れ線
            val path = Path()
            entries.forEachIndexed { i, e ->
                val x = xFor(i); val y = yFor(e.weightKg)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, primary, style = Stroke(width = 2.5.dp.toPx()))

            // ポイント
            entries.forEachIndexed { i, e ->
                drawCircle(primary, radius = 4.dp.toPx(), center = Offset(xFor(i), yFor(e.weightKg)))
            }
        }
    }
}
