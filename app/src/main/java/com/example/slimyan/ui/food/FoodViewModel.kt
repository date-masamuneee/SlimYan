package com.example.slimyan.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.Food
import com.example.slimyan.data.remote.AiRepository
import com.example.slimyan.data.remote.NutritionEstimate
import com.example.slimyan.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EstimateState {
    object Idle : EstimateState()
    object Loading : EstimateState()
    data class Done(val estimate: NutritionEstimate) : EstimateState()
    data class Error(val msg: String) : EstimateState()
}

data class FoodUiState(
    val foods: List<Food> = emptyList(),
    val query: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val repo: FoodRepository,
    private val aiRepo: AiRepository,
) : ViewModel() {

    private val _estimate = MutableStateFlow<EstimateState>(EstimateState.Idle)
    val estimate: StateFlow<EstimateState> = _estimate.asStateFlow()

    fun requestEstimate(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _estimate.value = EstimateState.Loading
            aiRepo.estimateNutrition(name)
                .onSuccess { _estimate.value = EstimateState.Done(it) }
                .onFailure { _estimate.value = EstimateState.Error(it.message ?: "推定エラー") }
        }
    }

    fun clearEstimate() { _estimate.value = EstimateState.Idle }

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
        calories: Float,
        protein: Float,
        fat: Float,
        carb: Float,
        servingGrams: Float,
        isFavorite: Boolean,
    ) {
        viewModelScope.launch {
            repo.save(Food(
                name = name.trim(),
                calories = calories,
                protein = protein,
                fat = fat,
                carb = carb,
                servingGrams = servingGrams,
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
