package com.example.petbuddy.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Event(
    val eventId: String = "",
    val title: String = "",
    val tag: String? = null,
    val isAllDay: Boolean = false,
    val startDate: Timestamp = Timestamp.now(),  // เปลี่ยนเป็น Timestamp
    val endDate: Timestamp? = null,              // เผื่อ event ข้ามวัน
    val place: String? = null,
    val note: String? = null,
    val petIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) : Serializable {
    val startDateString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return format.format(startDate.toDate())
        }

    val startDateOnlyString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(startDate.toDate())
        }

    val startTimeString: String
        get() {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(startDate.toDate())
        }
}
