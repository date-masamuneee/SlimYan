package com.example.slimyan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = if (state.goalEpochDay > 0L)
                state.goalEpochDay * 86_400_000L else null
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        vm.setGoalDate(ms / 86_400_000L)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") }
            }
        ) { DatePicker(state = dpState) }
    }

    if (state.isSaved) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            vm.update { copy(isSaved = false) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("設定・プロフィール") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionLabel("基本情報")

            NumberField("身長 (cm)", state.heightCm) { vm.update { copy(heightCm = it) } }
            NumberField("現在の体重 (kg)", state.weightKg) { vm.update { copy(weightKg = it) } }
            NumberField("年齢", state.age) { vm.update { copy(age = it) } }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("male" to "男性", "female" to "女性").forEach { (key, label) ->
                    FilterChip(
                        selected = state.sex == key,
                        onClick = { vm.update { copy(sex = key) } },
                        label = { Text(label) }
                    )
                }
            }

            val activityOptions = listOf(
                "sedentary" to "座り仕事中心",
                "light" to "軽い運動（週1-2）",
                "moderate" to "適度な運動（週3-5）",
                "active" to "ハードな運動（週6-7）",
                "very_active" to "超ハード or 肉体労働",
            )
            var expandActivity by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandActivity,
                onExpandedChange = { expandActivity = it }
            ) {
                OutlinedTextField(
                    value = activityOptions.firstOrNull { it.first == state.activityLevel }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("活動量") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandActivity) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandActivity,
                    onDismissRequest = { expandActivity = false }
                ) {
                    activityOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { vm.update { copy(activityLevel = key) }; expandActivity = false }
                        )
                    }
                }
            }

            if (state.tdee > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TDEE: ${state.tdee} kcal/日", style = MaterialTheme.typography.titleMedium)
                        if (state.calculatedTarget > 0)
                            Text("摂取目標: ${state.calculatedTarget} kcal/日", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            SectionLabel("目標")

            NumberField("目標体重 (kg)", state.goalWeightKg) { vm.update { copy(goalWeightKg = it) } }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("達成期日: ${state.goalDateLabel}")
            }

            SectionLabel("目標PFC (g/日)")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("P", state.proteinTargetG, Modifier.weight(1f)) {
                    vm.update { copy(proteinTargetG = it) }
                }
                NumberField("F", state.fatTargetG, Modifier.weight(1f)) {
                    vm.update { copy(fatTargetG = it) }
                }
                NumberField("C", state.carbTargetG, Modifier.weight(1f)) {
                    vm.update { copy(carbTargetG = it) }
                }
            }

            Button(
                onClick = { vm.save() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaved) "保存済み ✓" else "保存")
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            ReminderSection()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier,
    )
}
