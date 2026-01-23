package com.rohan.timetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = TimetableRepository(application)

    // Currently selected day (Monday, Tuesday, ...)
    private val _selectedDay = MutableStateFlow("Monday")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Classes for the selected day
    val classesForDay: StateFlow<List<ClassEntity>> =
        _selectedDay
            .flatMapLatest { day ->
                repository.getClassesForDay(day)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // 🔹 Called when user selects a day from drawer
    fun setDay(day: String) {
        _selectedDay.value = day
    }

    // 🔹 Add a class
    fun addClass(entry: ClassEntity) {
        viewModelScope.launch {
            repository.addClass(entry)
        }
    }

    // 🔹 Update a class
    fun updateClass(entry: ClassEntity) {
        viewModelScope.launch {
            repository.updateClass(entry)
        }
    }

    // 🔹 Delete a class
    fun deleteClass(entry: ClassEntity) {
        viewModelScope.launch {
            repository.deleteClass(entry)
        }
    }

    // 🔹 Optional: clear everything
    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}