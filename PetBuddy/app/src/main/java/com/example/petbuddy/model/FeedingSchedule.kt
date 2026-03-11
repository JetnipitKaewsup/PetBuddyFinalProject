package com.example.petbuddy.model

import com.google.firebase.Timestamp

data class FeedingSchedule(

    val id: String = "",

    val title: String = "",
    val note: String = "",
    val type: String = "",

    val hour: Int = 0,
    val minute: Int = 0,

    val repeatType: String = "once",
    val days: List<String> = emptyList(),

    val petIds: List<String> = emptyList(),

    val isActive: Boolean = true,

    val createdAt: Timestamp? = null
)