package com.example.petbuddy.model

import java.io.Serializable
import java.util.Date

data class WeightRecord(
    val id: String = "",              // document id ใน Firestore
    val petId: String = "",            // id ของสัตว์เลี้ยง
    val weight: Double = 0.0,          // น้ำหนัก
    val timestamp: Long = System.currentTimeMillis(), // เก็บเป็น timestamp (milliseconds)
    val note: String? = null,          // หมายเหตุ (ถ้ามี)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    // computed properties สำหรับ convenience
    val date: Date
        get() = Date(timestamp)

    val dateString: String
        get() {
            val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val timeString: String
        get() {
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val dateTimeString: String
        get() {
            val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return format.format(Date(timestamp))
        }
}