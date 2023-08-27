package com.example.meddirect.model

data class Appointment(
    val appointmentId: String? = null,
    val userId: String? = null,
    val doctorId: String? = null,
    val date: String? = null,
    val time: String? = null,
    val description: String? = null,
    val totalPay: String? = null,
    val paymentId: String? = null
)
