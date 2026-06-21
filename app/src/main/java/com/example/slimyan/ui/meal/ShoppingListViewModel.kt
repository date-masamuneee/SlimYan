package com.example.slimyan.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.ShoppingListCalculator
import com.example.slimyan.data.repository.FoodRepository
import com.example.slimyan.data.repository.MealTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ShoppingUiState(
    val lines: List<ShoppingListCalculator.Line> = emptyList(),
    val weeks: Int = 1,
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val templateRepo: MealTemplateRepository,
    private val foodRepo: FoodRepository,
) : ViewModel() {

    private val _weeks = MutableStateFlow(1)

    val state: StateFlow<ShoppingUiState> = combine(
        templateRepo.getAll(),
        foodRepo.getAll(),
        _weeks,
    ) { items, foods, weeks ->
        ShoppingUiState(
            lines = ShoppingListCalculator.calc(items, foods.associateBy { it.id }, weeks),
            weeks = weeks,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShoppingUiState())

    fun setWeeks(n: Int) { _weeks.value = n.coerceIn(1, 12) }
}
