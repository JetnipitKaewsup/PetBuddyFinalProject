package com.example.petbuddy.model

import java.io.Serializable
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

data class VaccinationRecord(
    val id: String = "",
    val petId: String = "",
    val vaccineName: String = "",
    val dose: Int = 1,
    val timestamp: Long = System.currentTimeMillis(), // วันที่ฉีด
    val place: String? = null,
    val nextDueDate: Long? = null, // วันที่นัดฉีดครั้งต่อไป (optional)
    val nextDose: Int? = null, // เข็มถัดไป (optional)
    val nextVaccineName: String? = null, // ชื่อวัคซีนเข็มถัดไป (optional)
    val nextPlace: String? = null, // สถานที่นัดครั้งต่อไป (optional)
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    val date: Date
        get() = Date(timestamp)

    val dateString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val timeString: String
        get() {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val nextDueDateString: String?
        get() = nextDueDate?.let {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(Date(it))
        }

    val daysUntilNext: Int?
        get() = nextDueDate?.let {
            val today = System.currentTimeMillis()
            val diff = it - today
            (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    val isNextDueSoon: Boolean
        get() = daysUntilNext?.let { it in 0..7 } ?: false

    val isOverdue: Boolean
        get() = daysUntilNext?.let { it < 0 } ?: false
}