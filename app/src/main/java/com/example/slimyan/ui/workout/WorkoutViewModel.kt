package com.example.slimyan.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.Exercise
import com.example.slimyan.data.entity.SetEntry
import com.example.slimyan.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WorkoutUiState(
    val exercises: List<Exercise> = emptyList(),
    val todaySets: List<SetEntry> = emptyList(),
    val prevSets: List<SetEntry> = emptyList(),
    val selectedExerciseId: Long? = null,
    val weightInput: String = "",
    val repsInput: String = "10",
    val showAddExercise: Boolean = false,
    val newExerciseName: String = "",
    val newMuscleGroup: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository,
) : ViewModel() {

    private val _sel = MutableStateFlow<Long?>(null)
    private val _weight = MutableStateFlow("")
    private val _reps = MutableStateFlow("10")
    private val _showAdd = MutableStateFlow(false)
    private val _newName = MutableStateFlow("")
    private val _newGroup = MutableStateFlow("")

    val state: StateFlow<WorkoutUiState> = combine(
        repo.getAllExercises(),
        _sel,
        _weight,
        _reps,
        _showAdd,
    ) { exs, sel, w, r, show -> Quintet(exs, sel, w, r, show) }
        .flatMapLatest { (exs, sel, w, r, show) ->
            val today = LocalDate.now().toEpochDay()
            val todayFlow = repo.getSetsForDay(today)
            val prevFlow = if (sel != null) repo.getRecentSetsForExercise(sel)
            else flowOf(emptyList())
            combine(todayFlow, prevFlow, _newName, _newGroup) { today2, prev, name, group ->
                WorkoutUiState(
                    exercises = exs,
                    todaySets = today2,
                    prevSets = prev.filter { it.dateEpochDay < LocalDate.now().toEpochDay() }.take(5),
                    selectedExerciseId = sel,
                    weightInput = w,
                    repsInput = r,
                    showAddExercise = show,
                    newExerciseName = name,
                    newMuscleGroup = group,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkoutUiState())

    fun selectExercise(id: Long?) {
        _sel.value = id
        _weight.value = ""
    }

    fun setWeight(v: String) { _weight.value = v }
    fun setReps(v: String) { _reps.value = v }
    fun showAddExercise(show: Boolean) { _showAdd.value = show }
    fun setNewExerciseName(v: String) { _newName.value = v }
    fun setNewMuscleGroup(v: String) { _newGroup.value = v }

    fun addSet() {
        val exId = _sel.value ?: return
        val weight = _weight.value.toFloatOrNull() ?: return
        val reps = _reps.value.toIntOrNull() ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val existingCount = repo.getSetsForDay(today).first()
                .count { it.exerciseId == exId }
            repo.addSet(
                SetEntry(
                    exerciseId = exId,
                    dateEpochDay = today,
                    weight = weight,
                    reps = reps,
                    setOrder = existingCount + 1,
                )
            )
        }
    }

    fun deleteSet(entry: SetEntry) {
        viewModelScope.launch { repo.deleteSet(entry) }
    }

    fun addExercise() {
        val name = _newName.value.trim()
        val group = _newGroup.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.addExercise(Exercise(name = name, muscleGroup = group.ifBlank { "その他" }))
            _newName.value = ""
            _newGroup.value = ""
            _showAdd.value = false
        }
    }
}

private data class Quintet<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
private operator fun <A, B, C, D, E> Quintet<A, B, C, D, E>.component1() = a
private operator fun <A, B, C, D, E> Quintet<A, B, C, D, E>.component2() = b
private operator fun <A, B, C, D, E> Quintet<A, B, C, D, E>.component3() = c
private operator fun <A, B, C, D, E> Quintet<A, B, C, D, E>.component4() = d
private operator fun <A, B, C, D, E> Quintet<A, B, C, D, E>.component5() = e
