package com.example.meddirect.model

data class TimeSlot(
    val hour: String? = null,
    val minutes: String? = "00",
    var isSelected: Boolean = false,
    var isBooked: Boolean = false
)