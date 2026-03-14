package com.example.petbuddy.model

data class VaccinationStatusCard(
    val name: String,
    val isCompleted: Boolean,
    val nextDueDate: String? = null
)
