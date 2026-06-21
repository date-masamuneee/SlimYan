package com.example.slimyan.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.ProgressionAdvisor
import com.example.slimyan.data.entity.Exercise
import com.example.slimyan.data.entity.SetEntry
import com.example.slimyan.data.entity.WorkoutProgramItem
import com.example.slimyan.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgramRow(
    val item: WorkoutProgramItem,
    val exercise: Exercise?,
    val shouldIncrease: Boolean,
    val lastSession: List<SetEntry>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutProgramViewModel @Inject constructor(
    private val repo: WorkoutRepository,
) : ViewModel() {

    val rows: StateFlow<List<ProgramRow>> =
        combine(repo.getProgram(), repo.getAllExercises()) { items, exs -> items to exs }
            .mapLatest { (items, exs) ->
                val byId = exs.associateBy { it.id }
                items.map { item ->
                    val recent = repo.getRecentSetsForExercise(item.exerciseId).first()
                    val lastDate = recent.maxOfOrNull { it.dateEpochDay }
                    val lastSession = recent.filter { it.dateEpochDay == lastDate }
                    ProgramRow(
                        item = item,
                        exercise = byId[item.exerciseId],
                        shouldIncrease = ProgressionAdvisor.shouldIncreaseWeight(
                            lastSession, item.targetSets, item.repCeiling
                        ),
                        lastSession = lastSession,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = repo.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToProgram(exerciseId: Long, targetSets: Int, repCeiling: Int) {
        viewModelScope.launch {
            repo.addProgramItem(
                WorkoutProgramItem(
                    exerciseId = exerciseId,
                    targetSets = targetSets,
                    repCeiling = repCeiling,
                )
            )
        }
    }

    fun removeFromProgram(item: WorkoutProgramItem) {
        viewModelScope.launch { repo.deleteProgramItem(item) }
    }
}
