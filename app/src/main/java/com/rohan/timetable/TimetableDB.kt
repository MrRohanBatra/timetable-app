package com.rohan.timetable

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClassEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TimetableDatabase : RoomDatabase() {

    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: TimetableDatabase? = null

        fun getInstance(context: Context): TimetableDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TimetableDatabase::class.java,
                    "timetable_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}