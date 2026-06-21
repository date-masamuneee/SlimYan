package com.example.slimyan.ui.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.TemplateSummary
import com.example.slimyan.data.entity.MealTemplateItem
import com.example.slimyan.data.remote.AiRepository
import com.example.slimyan.data.remote.AuditRequestBuilder
import com.example.slimyan.data.remote.MealPlanAudit
import com.example.slimyan.data.repository.FoodRepository
import com.example.slimyan.data.repository.MealTemplateRepository
import com.example.slimyan.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuditState {
    object Idle : AuditState()
    object Loading : AuditState()
    data class Ready(
        val audit: MealPlanAudit,
        val before: TemplateSummary.Totals,
        val after: TemplateSummary.Totals?,
    ) : AuditState()
    object Applied : AuditState()
    data class Error(val msg: String) : AuditState()
}

@HiltViewModel
class MealAuditViewModel @Inject constructor(
    private val templateRepo: MealTemplateRepository,
    private val foodRepo: FoodRepository,
    private val profileRepo: ProfileRepository,
    private val aiRepo: AiRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuditState>(AuditState.Idle)
    val state: StateFlow<AuditState> = _state.asStateFlow()

    fun runAudit(refinement: String? = null) {
        viewModelScope.launch {
            _state.value = AuditState.Loading
            val items = templateRepo.getAll().first()
            val profile = profileRepo.get().first()
            if (profile == null || profile.dailyCalorieTarget <= 0) {
                _state.value = AuditState.Error("先に設定で目標カロリーを入れてくれ"); return@launch
            }
            if (items.isEmpty()) {
                _state.value = AuditState.Error("週間テンプレが空だよ"); return@launch
            }
            val byId = foodRepo.getAll().first().associateBy { it.id }
            val prompt = AuditRequestBuilder.build(items, byId, profile, refinement)
            aiRepo.auditMealPlan(prompt)
                .onSuccess { audit ->
                    val before = TemplateSummary.totals(items, byId)
                    val after = if (audit.revised.isNotEmpty())
                        TemplateSummary.totals(audit.revised.map { it.toTemplateItem() }, byId)
                    else null
                    _state.value = AuditState.Ready(audit, before, after)
                }
                .onFailure { _state.value = AuditState.Error(it.message ?: "監査エラー") }
        }
    }

    fun applyRevision() {
        val ready = _state.value as? AuditState.Ready ?: return
        if (ready.audit.revised.isEmpty()) return
        viewModelScope.launch {
            val newItems = ready.audit.revised
                .groupBy { it.dayOfWeek to it.mealSlot }
                .flatMap { (_, group) ->
                    group.mapIndexed { idx, r ->
                        MealTemplateItem(
                            dayOfWeek = r.dayOfWeek,
                            mealSlot = r.mealSlot,
                            foodId = r.foodId,
                            grams = r.grams,
                            sortOrder = idx,
                        )
                    }
                }
            templateRepo.replaceAll(newItems)
            _state.value = AuditState.Applied
        }
    }

    fun dismiss() { _state.value = AuditState.Idle }
}

private fun com.example.slimyan.data.remote.RevisedTemplateItem.toTemplateItem() =
    MealTemplateItem(dayOfWeek = dayOfWeek, mealSlot = mealSlot, foodId = foodId, grams = grams)
