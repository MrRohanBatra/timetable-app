package com.rohan.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable")
data class ClassEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Monday, Tuesday, ...
    val day: String,

    // "8:00 AM - 9:00 AM"
    val time: String,

    // "Operations Research"
    val subjectName: String,

    // "FF9", "G2", etc.
    val classroom: String,

    // L / P / T
    val classType: String
)