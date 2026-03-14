package com.example.petbuddy.model

data class VaccinationDetail(
    val vaccineName: String,
    val dose: Int,
    val date: String,
    val time: String,
    val place: String? = null
)
