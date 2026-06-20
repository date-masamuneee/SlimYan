package com.example.slimyan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.TdeeCalculator
import com.example.slimyan.data.entity.UserProfile
import com.example.slimyan.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsUiState(
    val heightCm: String = "170",
    val weightKg: String = "65",
    val age: String = "30",
    val sex: String = "male",
    val activityLevel: String = "moderate",
    val goalWeightKg: String = "60",
    val goalEpochDay: Long = 0L,
    val goalDateLabel: String = "未設定",
    val proteinTargetG: String = "0",
    val fatTargetG: String = "0",
    val carbTargetG: String = "0",
    val tdee: Int = 0,
    val calculatedTarget: Int = 0,
    val isSaved: Boolean = false,
)

private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.get().collect { profile ->
                if (profile != null) _state.update { it.fromProfile(profile) }
            }
        }
    }

    fun update(block: SettingsUiState.() -> SettingsUiState) {
        _state.update { it.block().recalculate() }
    }

    fun setGoalDate(epochDay: Long) {
        val label = runCatching {
            LocalDate.ofEpochDay(epochDay).format(DATE_FMT)
        }.getOrDefault("未設定")
        _state.update { it.copy(goalEpochDay = epochDay, goalDateLabel = label).recalculate() }
    }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            repo.save(
                UserProfile(
                    id = 1,
                    heightCm = s.heightCm.toFloatOrNull() ?: 170f,
                    weightKg = s.weightKg.toFloatOrNull() ?: 65f,
                    age = s.age.toIntOrNull() ?: 30,
                    sex = s.sex,
                    activityLevel = s.activityLevel,
                    goalWeightKg = s.goalWeightKg.toFloatOrNull() ?: 60f,
                    goalDate = s.goalEpochDay,
                    dailyCalorieTarget = s.calculatedTarget,
                    proteinTargetG = s.proteinTargetG.toFloatOrNull() ?: 0f,
                    fatTargetG = s.fatTargetG.toFloatOrNull() ?: 0f,
                    carbTargetG = s.carbTargetG.toFloatOrNull() ?: 0f,
                )
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun SettingsUiState.fromProfile(p: UserProfile): SettingsUiState {
        val label = if (p.goalDate > 0L) runCatching {
            LocalDate.ofEpochDay(p.goalDate).format(DATE_FMT)
        }.getOrDefault("未設定") else "未設定"
        return copy(
            heightCm = p.heightCm.toString(),
            weightKg = p.weightKg.toString(),
            age = p.age.toString(),
            sex = p.sex,
            activityLevel = p.activityLevel,
            goalWeightKg = p.goalWeightKg.toString(),
            goalEpochDay = p.goalDate,
            goalDateLabel = label,
            proteinTargetG = p.proteinTargetG.toString(),
            fatTargetG = p.fatTargetG.toString(),
            carbTargetG = p.carbTargetG.toString(),
        ).recalculate()
    }

    private fun SettingsUiState.recalculate(): SettingsUiState {
        val weight = weightKg.toFloatOrNull() ?: return this
        val height = heightCm.toFloatOrNull() ?: return this
        val ageInt = age.toIntOrNull() ?: return this
        val bmr = TdeeCalculator.bmr(weight, height, ageInt, sex)
        val tdeeVal = TdeeCalculator.tdee(bmr, activityLevel)
        val target = TdeeCalculator.dailyTarget(
            tdeeVal,
            weight,
            goalWeightKg.toFloatOrNull() ?: weight,
            goalEpochDay,
        )
        return copy(tdee = tdeeVal.toInt(), calculatedTarget = target)
    }
}
