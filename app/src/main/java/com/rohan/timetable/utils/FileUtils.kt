//package com.rohan.jetpacklearn.utils
//
//import android.content.Context
//import com.rohan.jetpacklearn.ClassEntry
//import com.rohan.jetpacklearn.R
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.json.Json
//
//object FileUtils {
//
//    // Reads "res/raw/timetable.json" and parses it
//    suspend fun loadTimetable(context: Context): Map<String, List<ClassEntry>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                // 1. Open the file
//                val inputStream = context.resources.openRawResource(R.raw.timetable)
//
//                // 2. Read content
//                val jsonString = inputStream.bufferedReader().use { it.readText() }
//
//                // 3. Parse JSON -> Map<String, List<ClassEntry>>
//                // This matches exactly the {"Monday": [ ... ], "Tuesday": [ ... ]} format
//                Json.decodeFromString(jsonString)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                emptyMap() // Return empty if file is missing or broken
//            }
//        }
//    }
//}