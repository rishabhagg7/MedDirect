package com.example.meddirect.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDate(var data: Date, var isSelected: Boolean = false) {

    val calendarDay: String
        get() = SimpleDateFormat("EE", Locale.getDefault()).format(data)

    val calendarDate: String
        get() {
            val cal = Calendar.getInstance()
            cal.time = data
            return cal[Calendar.DAY_OF_MONTH].toString()
        }
    val calendarDayOfWeek: String
        get() = SimpleDateFormat("EEEE",Locale.getDefault()).format(data)

    val calendarMonth: String
        get() = SimpleDateFormat("LLLL",Locale.getDefault()).format(data)

    val calendarYear: String
        get() = SimpleDateFormat("YYYY",Locale.getDefault()).format(data)
}