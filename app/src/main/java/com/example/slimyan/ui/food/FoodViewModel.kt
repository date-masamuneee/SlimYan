package com.example.slimyan.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodUiState(
    val foods: List<Food> = emptyList(),
    val query: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val repo: FoodRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val state: StateFlow<FoodUiState> = _query
        .flatMapLatest { q ->
            if (q.isBlank()) repo.getAll() else repo.search(q)
        }
        .combine(_query) { foods, q -> FoodUiState(foods = foods, query = q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodUiState())

    fun setQuery(q: String) { _query.value = q }

    fun add(
        name: String,
        caloriesPer100g: Float,
        proteinPer100g: Float,
        fatPer100g: Float,
        carbPer100g: Float,
        defaultGrams: Float,
        isFavorite: Boolean,
    ) {
        viewModelScope.launch {
            repo.save(Food(
                name = name.trim(),
                caloriesPer100g = caloriesPer100g,
                proteinPer100g = proteinPer100g,
                fatPer100g = fatPer100g,
                carbPer100g = carbPer100g,
                defaultGrams = defaultGrams,
                isFavorite = isFavorite,
            ))
        }
    }

    fun update(food: Food) {
        viewModelScope.launch { repo.update(food) }
    }

    fun toggleFavorite(food: Food) {
        viewModelScope.launch { repo.update(food.copy(isFavorite = !food.isFavorite)) }
    }

    fun delete(food: Food) {
        viewModelScope.launch { repo.delete(food) }
    }
}
