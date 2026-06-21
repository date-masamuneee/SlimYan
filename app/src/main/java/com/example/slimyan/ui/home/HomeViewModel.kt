package com.example.slimyan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.data.entity.SetEntry
import com.example.slimyan.data.entity.UserProfile
import com.example.slimyan.data.remote.AiRepository
import com.example.slimyan.data.remote.RecoverySuggestion
import com.example.slimyan.data.repository.BodyWeightRepository
import com.example.slimyan.data.repository.MealRepository
import com.example.slimyan.data.repository.ProfileRepository
import com.example.slimyan.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class RecoveryState {
    object Idle : RecoveryState()
    object Loading : RecoveryState()
    data class Success(val suggestion: RecoverySuggestion) : RecoveryState()
    data class Error(val message: String) : RecoveryState()
}

data class HomeUiState(
    val profile: UserProfile? = null,
    val dailyCalorieTarget: Int = 0,
    val checkedCalories: Float = 0f,
    val todayEntries: List<MealEntry> = emptyList(),
    val recentWeights: List<BodyWeight> = emptyList(),
    val todaySets: List<SetEntry> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    mealRepo: MealRepository,
    weightRepo: BodyWeightRepository,
    workoutRepo: WorkoutRepository,
    private val aiRepo: AiRepository,
) : ViewModel() {

    private val _recovery = MutableStateFlow<RecoveryState>(RecoveryState.Idle)
    val recovery: StateFlow<RecoveryState> = _recovery.asStateFlow()

    val state: StateFlow<HomeUiState> = run {
        val today = LocalDate.now().toEpochDay()
        val week = LocalDate.now().minusDays(7).toEpochDay()
        combine(
            profileRepo.get(),
            mealRepo.getForDay(today),
            mealRepo.checkedCaloriesForDay(today),
            weightRepo.getSince(week),
            workoutRepo.getSetsForDay(today),
        ) { profile, entries, checked, weights, sets ->
            HomeUiState(
                profile = profile,
                dailyCalorieTarget = profile?.dailyCalorieTarget ?: 0,
                checkedCalories = checked,
                todayEntries = entries,
                recentWeights = weights,
                todaySets = sets,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun requestRecovery() {
        val s = state.value
        val profile = s.profile ?: return
        if (s.dailyCalorieTarget <= 0) return
        viewModelScope.launch {
            _recovery.value = RecoveryState.Loading
            aiRepo.getRecoverySuggestion(
                targetKcal = s.dailyCalorieTarget,
                consumedKcal = s.checkedCalories,
                proteinTargetG = profile.proteinTargetG,
                fatTargetG = profile.fatTargetG,
                carbTargetG = profile.carbTargetG,
            ).onSuccess { _recovery.value = RecoveryState.Success(it) }
             .onFailure { _recovery.value = RecoveryState.Error(it.message ?: "エラーが発生しました") }
        }
    }

    fun dismissRecovery() { _recovery.value = RecoveryState.Idle }
}
