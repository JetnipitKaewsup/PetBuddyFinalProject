package com.example.petbuddy.model

import java.security.Timestamp

data class FeedingRecords(

    val id: String = "",

    val scheduleId: String = "",
    val petId: String = "",

    val foodType: String = "",
    val note: String = "",

    val fedAt: Timestamp? = null
)
