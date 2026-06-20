package com.example.slimyan.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.BodyWeight
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.data.entity.SetEntry
import com.example.slimyan.data.repository.BodyWeightRepository
import com.example.slimyan.data.repository.MealRepository
import com.example.slimyan.data.repository.ProfileRepository
import com.example.slimyan.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
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
) : ViewModel() {

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
                dailyCalorieTarget = profile?.dailyCalorieTarget ?: 0,
                checkedCalories = checked,
                todayEntries = entries,
                recentWeights = weights,
                todaySets = sets,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
