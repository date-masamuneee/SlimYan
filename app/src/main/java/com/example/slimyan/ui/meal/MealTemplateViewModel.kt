package com.example.slimyan.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.TemplateSummary
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.repository.FoodRepository
import com.example.slimyan.data.repository.MealTemplateRepository
import com.example.slimyan.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TemplateUiState(
    val selectedDay: Int = LocalDate.now().dayOfWeek.value, // 1=月..7=日
    val dayItems: List<MealTemplateItem> = emptyList(),
    val foodsById: Map<Long, Food> = emptyMap(),
    val allFoods: List<Food> = emptyList(),
    val dayTotals: TemplateSummary.Totals = TemplateSummary.Totals(),
    val weekTotals: TemplateSummary.Totals = TemplateSummary.Totals(),
    val dailyTarget: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealTemplateViewModel @Inject constructor(
    private val templateRepo: MealTemplateRepository,
    private val foodRepo: FoodRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)

    val state: StateFlow<TemplateUiState> = _selectedDay
        .flatMapLatest { day ->
            combine(
                templateRepo.getForDay(day),
                templateRepo.getAll(),
                foodRepo.getAll(),
                profileRepo.get(),
            ) { dayItems, allItems, foods, profile ->
                val byId = foods.associateBy { it.id }
                TemplateUiState(
                    selectedDay = day,
                    dayItems = dayItems,
                    foodsById = byId,
                    allFoods = foods,
                    dayTotals = TemplateSummary.totals(dayItems, byId),
                    weekTotals = TemplateSummary.totals(allItems, byId),
                    dailyTarget = profile?.dailyCalorieTarget ?: 0,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TemplateUiState())

    fun selectDay(day: Int) { _selectedDay.value = day }

    fun addItem(slot: String, foodId: Long, grams: Float) {
        val day = _selectedDay.value
        viewModelScope.launch {
            val existing = templateRepo.getForDayOnce(day).count { it.mealSlot == slot }
            templateRepo.add(
                MealTemplateItem(
                    dayOfWeek = day,
                    mealSlot = slot,
                    foodId = foodId,
                    grams = grams,
                    sortOrder = existing,
                )
            )
        }
    }

    fun deleteItem(item: MealTemplateItem) {
        viewModelScope.launch { templateRepo.delete(item) }
    }
}
