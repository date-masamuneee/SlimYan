package com.example.slimyan.ui.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ShoppingListContent(vm: ShoppingListViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 週数ステッパー
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("まとめ買い:", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { vm.setWeeks(state.weeks - 1) }) {
                Icon(Icons.Filled.Remove, "減らす")
            }
            Text("${state.weeks} 週分", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { vm.setWeeks(state.weeks + 1) }) {
                Icon(Icons.Filled.Add, "増やす")
            }
        }

        if (state.lines.isEmpty()) {
            Text(
                "週間テンプレに品目を追加すると、ここに買い物リストが出るよ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        state.lines.forEach { line ->
            Card {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(line.food.name, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    Text(
                        "×${formatCount(line.servings)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("(${line.totalGrams.toInt()}g)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun formatCount(servings: Float): String {
    val rounded = Math.round(servings * 10f) / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}
