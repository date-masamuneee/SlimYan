@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.DayMask
import com.example.slimyan.data.entity.Reminder
import com.example.slimyan.ui.meal.MEAL_SLOTS

private val DAY_LABELS = listOf("月", "火", "水", "木", "金", "土", "日")

@Composable
fun ReminderSection(vm: ReminderViewModel = hiltViewModel()) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Reminder?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        ReminderEditDialog(
            initial = editing,
            onConfirm = { vm.save(it); showEditor = false; editing = null },
            onDismiss = { showEditor = false; editing = null }
        )
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("リマインド", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        FilledTonalButton(onClick = { editing = null; showEditor = true }) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("追加")
        }
    }

    if (reminders.isEmpty()) {
        Text("食事・筋トレの通知を追加できるよ", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline)
    }

    reminders.forEach { r ->
        Card {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(1f).clickable { editing = r; showEditor = true }
                ) {
                    Text(
                        "%02d:%02d  %s".format(r.hour, r.minute, r.label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "${if (r.type == "workout") "筋トレ" else "食事"} · ${daysSummary(r.daysOfWeekMask)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(checked = r.enabled, onCheckedChange = { vm.toggleEnabled(r) })
                IconButton(onClick = { vm.delete(r) }) {
                    Icon(Icons.Filled.Delete, "削除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun daysSummary(mask: Int): String {
    val days = DayMask.toDays(mask)
    if (days.size == 7) return "毎日"
    if (days.isEmpty()) return "曜日未設定"
    return days.sorted().joinToString("") { DAY_LABELS[it] }
}

@Composable
private fun ReminderEditDialog(
    initial: Reminder?,
    onConfirm: (Reminder) -> Unit,
    onDismiss: () -> Unit,
) {
    var type by remember { mutableStateOf(initial?.type ?: "meal") }
    var mealSlot by remember { mutableStateOf(initial?.mealSlot ?: "breakfast") }
    var label by remember { mutableStateOf(initial?.label ?: "朝食の時間") }
    var mask by remember { mutableStateOf(initial?.daysOfWeekMask ?: DayMask.ALL) }
    val timeState = rememberTimePickerState(
        initialHour = initial?.hour ?: 8,
        initialMinute = initial?.minute ?: 0,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "リマインド追加" else "リマインド編集") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 種別
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("meal" to "食事", "workout" to "筋トレ").forEach { (key, lbl) ->
                        FilterChip(selected = type == key, onClick = { type = key }, label = { Text(lbl) })
                    }
                }
                // 食事枠
                if (type == "meal") {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MEAL_SLOTS.forEach { (slot, slabel) ->
                            FilterChip(
                                selected = mealSlot == slot,
                                onClick = { mealSlot = slot },
                                label = { Text(slabel) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text("通知文言") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // 時刻
                TimeInput(state = timeState)
                // 曜日トグル
                Text("曜日", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    DAY_LABELS.forEachIndexed { i, d ->
                        FilterChip(
                            selected = DayMask.isSet(mask, i),
                            onClick = { mask = DayMask.toggle(mask, i) },
                            label = { Text(d) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        (initial ?: Reminder(
                            type = type, mealSlot = null, label = label,
                            hour = 0, minute = 0, daysOfWeekMask = 0
                        )).copy(
                            type = type,
                            mealSlot = if (type == "meal") mealSlot else null,
                            label = label,
                            hour = timeState.hour,
                            minute = timeState.minute,
                            daysOfWeekMask = mask,
                            enabled = initial?.enabled ?: true,
                        )
                    )
                },
                enabled = label.isNotBlank() && mask != 0
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
