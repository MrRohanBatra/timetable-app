package com.rohan.timetable

import android.content.Context
import kotlinx.coroutines.flow.Flow

class TimetableRepository(context: Context) {

    private val dao: TimetableDao =
        TimetableDatabase.getInstance(context).timetableDao()

    // 🔹 Read: observe classes for a specific day
    fun getClassesForDay(day: String): Flow<List<ClassEntity>> {
        return dao.getClassesForDay(day)
    }

    // 🔹 Create
    suspend fun addClass(entry: ClassEntity) {
        dao.insert(entry)
    }

    // 🔹 Update
    suspend fun updateClass(entry: ClassEntity) {
        dao.update(entry)
    }

    // 🔹 Delete
    suspend fun deleteClass(entry: ClassEntity) {
        dao.delete(entry)
    }

    suspend fun addAllClasses(entries: List<ClassEntity>) {
        dao.insertAll(entries)
    }
    suspend fun clearAll() {
        dao.clearAll()
    }
    suspend fun getAllClasses(): List<ClassEntity> {
        return dao.getAllClasses()
    }
}