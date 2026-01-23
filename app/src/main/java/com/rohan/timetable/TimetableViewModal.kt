package com.rohan.timetable

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    fun importFromJson(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("Import", "Starting JSON parsing...")

                val classesToInsert = mutableListOf<ClassEntity>()
                val jsonObject = JSONObject(jsonString)
                val daysIterator = jsonObject.keys()

                while (daysIterator.hasNext()) {
                    val dayName = daysIterator.next()
                    val classesArray = jsonObject.getJSONArray(dayName)

                    for (i in 0 until classesArray.length()) {
                        val classObj = classesArray.getJSONObject(i)

                        classesToInsert.add(
                            ClassEntity(
                                day = dayName,
                                time = classObj.optString("time", ""),
                                subjectName = classObj.optString("subjectName", ""),
                                classroom = classObj.optString("classroom", ""),
                                classType = classObj.optString("classType", "")
                            )
                        )
                    }
                }

                if (classesToInsert.isNotEmpty()) {
                    repository.clearAll()
                    repository.addAllClasses(classesToInsert)
                    Log.d("Import", "✅ Imported ${classesToInsert.size} classes")
                }

            } catch (e: Exception) {
                Log.e("ImportError", "Failed to import JSON", e)
            }
        }
    }
}
