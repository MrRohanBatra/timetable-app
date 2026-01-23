package com.rohan.timetable

import kotlinx.serialization.Serializable

@Serializable
data class ClassEntry(
    val time: String,       // e.g. "8:00 AM - 9:00 AM"
    val subjectName: String,
    val classroom: String,
    val classType: String   // "Lecture", "Lab", "Tutorial"
)


fun getClassType(l:String): String {
    var r:String="";
    r = when(l){
        "L"-> "Lecture"
        "T"-> "Tutorial"
        "P"-> "Practical"
        else-> "Error"

    }
    return r;
}