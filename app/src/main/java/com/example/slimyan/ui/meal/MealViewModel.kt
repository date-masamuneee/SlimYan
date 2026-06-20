package com.example.slimyan.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealEntry
import com.example.slimyan.data.repository.FoodRepository
import com.example.slimyan.data.repository.MealRepository
import com.example.slimyan.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MealUiState(
    val entries: List<MealEntry> = emptyList(),
    val foods: List<Food> = emptyList(),
    val foodQuery: String = "",
    val dailyTarget: Int = 0,
    val checkedCalories: Float = 0f,
)

val MEAL_SLOTS = listOf(
    "breakfast" to "朝食",
    "lunch" to "昼食",
    "dinner" to "夕食",
    "snack" to "間食",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealViewModel @Inject constructor(
    private val mealRepo: MealRepository,
    private val foodRepo: FoodRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val state: StateFlow<MealUiState> = combine(
        _query,
        profileRepo.get(),
    ) { q, profile -> q to (profile?.dailyCalorieTarget ?: 0) }
        .flatMapLatest { (q, target) ->
            val today = LocalDate.now().toEpochDay()
            combine(
                mealRepo.getForDay(today),
                mealRepo.checkedCaloriesForDay(today),
                if (q.isBlank()) foodRepo.getAll() else foodRepo.search(q),
            ) { entries, checked, foods ->
                MealUiState(
                    entries = entries,
                    foods = foods,
                    foodQuery = q,
                    dailyTarget = target,
                    checkedCalories = checked,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MealUiState())

    fun setQuery(q: String) { _query.value = q }

    fun addEntry(food: Food, slot: String, grams: Float) {
        val calories = food.caloriesPer100g * grams / 100f
        viewModelScope.launch {
            mealRepo.add(
                MealEntry(
                    dateEpochDay = LocalDate.now().toEpochDay(),
                    mealSlot = slot,
                    foodId = food.id,
                    foodName = food.name,
                    grams = grams,
                    calories = calories,
                )
            )
        }
    }

    fun toggleCheck(entry: MealEntry) {
        viewModelScope.launch { mealRepo.update(entry.copy(isChecked = !entry.isChecked)) }
    }

    fun delete(entry: MealEntry) {
        viewModelScope.launch { mealRepo.delete(entry) }
    }
}
