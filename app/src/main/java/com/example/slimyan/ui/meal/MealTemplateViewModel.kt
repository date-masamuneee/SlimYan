package com.example.slimyan.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.TemplateExpander
import com.example.slimyan.data.TemplateSummary
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.repository.FoodRepository
import com.example.slimyan.data.repository.MealRepository
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

sealed class ExpandEvent {
    object EmptyTemplate : ExpandEvent()
    object NeedConfirm : ExpandEvent()
    data class Done(val count: Int) : ExpandEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealTemplateViewModel @Inject constructor(
    private val templateRepo: MealTemplateRepository,
    private val foodRepo: FoodRepository,
    private val profileRepo: ProfileRepository,
    private val mealRepo: MealRepository,
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)

    private val _expandEvent = MutableStateFlow<ExpandEvent?>(null)
    val expandEvent: StateFlow<ExpandEvent?> = _expandEvent.asStateFlow()

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

    /** 今日の曜日のテンプレを当日の記録に展開。既存記録があれば確認を要求。 */
    fun requestExpandToday() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val items = templateRepo.getForDayOnce(today.dayOfWeek.value)
            if (items.isEmpty()) {
                _expandEvent.value = ExpandEvent.EmptyTemplate
                return@launch
            }
            val existing = mealRepo.getForDay(today.toEpochDay()).first()
            if (existing.isNotEmpty()) {
                _expandEvent.value = ExpandEvent.NeedConfirm
                return@launch
            }
            doExpand()
        }
    }

    fun confirmExpandToday() {
        viewModelScope.launch { doExpand() }
    }

    private suspend fun doExpand() {
        val today = LocalDate.now()
        val items = templateRepo.getForDayOnce(today.dayOfWeek.value)
        val foodsById = foodRepo.getAll().first().associateBy { it.id }
        val entries = TemplateExpander.expand(items, foodsById, today.toEpochDay())
        entries.forEach { mealRepo.add(it) }
        _expandEvent.value = ExpandEvent.Done(entries.size)
    }

    fun clearExpandEvent() { _expandEvent.value = null }
}
