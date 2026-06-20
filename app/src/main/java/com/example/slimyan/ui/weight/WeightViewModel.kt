package com.example.slimyan.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.data.repository.BodyWeightRepository
import com.example.slimyan.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class WeightPeriod { WEEK, MONTH }

data class WeightUiState(
    val entries: List<BodyWeight> = emptyList(),
    val period: WeightPeriod = WeightPeriod.WEEK,
    val inputKg: String = "",
    val goalWeightKg: Float = 0f,
    val todayKg: Float? = null,
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepo: BodyWeightRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(WeightPeriod.WEEK)
    private val _input = MutableStateFlow("")

    val state: StateFlow<WeightUiState> = combine(
        _period,
        _input,
        profileRepo.get(),
    ) { period, input, profile ->
        Triple(period, input, profile?.goalWeightKg ?: 0f)
    }.flatMapLatest { (period, input, goalWeight) ->
        val days = if (period == WeightPeriod.WEEK) 7L else 30L
        val from = LocalDate.now().minusDays(days).toEpochDay()
        weightRepo.getSince(from).map { entries ->
            val today = LocalDate.now().toEpochDay()
            WeightUiState(
                entries = entries,
                period = period,
                inputKg = input,
                goalWeightKg = goalWeight,
                todayKg = entries.firstOrNull { it.dateEpochDay == today }?.weightKg,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightUiState())

    fun setPeriod(p: WeightPeriod) { _period.value = p }

    fun setInput(v: String) { _input.value = v }

    fun save() {
        val kg = _input.value.toFloatOrNull() ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val existing = weightRepo.getForDay(today)
            if (existing != null) {
                weightRepo.delete(existing)
            }
            weightRepo.save(BodyWeight(dateEpochDay = today, weightKg = kg))
            _input.value = ""
        }
    }

    fun delete(entry: BodyWeight) {
        viewModelScope.launch { weightRepo.delete(entry) }
    }
}
