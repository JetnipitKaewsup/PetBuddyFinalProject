package com.example.petbuddy.model

data class FeedingRecord(

    val foodName: String = "",
    val foodType: String = "",
    val petIds: List<String> = emptyList(),

    val scheduleId: String = "",
    val fedAt: Long = 0,
    val dateTime: String = ""

)