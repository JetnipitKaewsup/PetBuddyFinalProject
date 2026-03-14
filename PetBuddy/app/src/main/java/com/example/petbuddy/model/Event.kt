package com.example.petbuddy.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Event(
    val eventId: String = "",
    val title: String = "",
    val tag: String? = null,
    val isAllDay: Boolean = false,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    val place: String? = null,
    val note: String? = null,
    val petIds: List<String> = emptyList(),
    val sourceType: String? = null, // "vaccination", "feeding", "custom"
    val sourceId: String? = null, // vaccination record ID
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val reminderBefore: Int = 0,
    val reminderEnabled: Boolean = false,
    val notificationId: Int = 0
) : Serializable {

    companion object {
        fun fromVaccination(
            vaccinationId: String,
            petId: String,
            vaccineName: String,
            dose: Int,
            dueDate: Timestamp,
            place: String? = null
        ): Event {
            return Event(
                eventId = UUID.randomUUID().toString(),
                title = "Vaccination: $vaccineName (Dose $dose)",
                tag = "Vaccination",
                isAllDay = false,
                startDate = dueDate,
                petIds = listOf(petId),
                place = place,
                sourceType = "vaccination",
                sourceId = vaccinationId,
                reminderEnabled = true,
                reminderBefore = 1440 // 1 day before
            )
        }
    }

    val startDateString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            return format.format(startDate.toDate())
        }

    val startDateOnlyString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            return format.format(startDate.toDate())
        }

    val startTimeString: String
        get() {
            val format = SimpleDateFormat("HH:mm", Locale.US)
            return format.format(startDate.toDate())
        }
}