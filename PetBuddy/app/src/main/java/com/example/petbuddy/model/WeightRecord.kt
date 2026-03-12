package com.example.petbuddy.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val timeString: String
        get() {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            return format.format(Date(timestamp))
        }

    val dateTimeString: String
        get() {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return format.format(Date(timestamp))
        }
}