package com.rohan.timetable

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetable WHERE day = :day ORDER BY time")
    fun getClassesForDay(day: String): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ClassEntity)

    @Update
    suspend fun update(entry: ClassEntity)

    @Delete
    suspend fun delete(entry: ClassEntity)

    @Query("DELETE FROM timetable")
    suspend fun clearAll()
}