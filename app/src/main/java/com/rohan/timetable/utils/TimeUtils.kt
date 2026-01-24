package com.rohan.timetable.utils

import com.rohan.timetable.ClassEntity
import com.rohan.timetable.ClassEntry
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object TimeUtils {
    // 1. Get today's day name (e.g. "Monday")
    fun getToday(): String {
        return LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    // 2. Check if a class is happening right now
    fun isLiveNow(timeRange: String, viewDay: String): Boolean {
        if (!viewDay.equals(getToday(), ignoreCase = true)) return false

        return try {
            val parts = timeRange.split("-").map { it.trim() }
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
            val start = LocalTime.parse(parts[0], formatter)
            val end = LocalTime.parse(parts[1], formatter)
            val now = LocalTime.now()

            !now.isBefore(start) && now.isBefore(end)
        } catch (e: Exception) {
            false
        }
    }

    fun sortClassesByTime(classes: List<ClassEntity>): List<ClassEntity> {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

        return classes.sortedBy { entry ->
            try {
                // Split "8:00 AM - 9:00 AM" and take the first part "8:00 AM"
                val startTimeString = entry.time.split("-")[0].trim()
                LocalTime.parse(startTimeString, formatter)
            } catch (e: Exception) {
                // Fallback: If parsing fails, put it at the end (MAX time)
                LocalTime.MAX
            }
        }
    }
}