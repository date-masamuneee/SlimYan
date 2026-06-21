package com.example.slimyan.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.slimyan.data.entity.Reminder
import com.example.slimyan.data.repository.ReminderRepository
import com.example.slimyan.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repo: ReminderRepository,
    private val scheduler: ReminderScheduler,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(reminder: Reminder) {
        viewModelScope.launch {
            val id = if (reminder.id == 0L) repo.add(reminder) else {
                repo.update(reminder); reminder.id
            }
            val saved = reminder.copy(id = id)
            if (saved.enabled) scheduler.schedule(appContext, saved)
            else scheduler.cancel(appContext, saved.id)
        }
    }

    fun toggleEnabled(reminder: Reminder) = save(reminder.copy(enabled = !reminder.enabled))

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            repo.delete(reminder)
            scheduler.cancel(appContext, reminder.id)
        }
    }
}
