package com.example.petbuddy.data

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val createAccount: Boolean = false,

    val petIds: List<String> = emptyList(),
    val weightIds: List<String> = emptyList(),
    val expenseIds: List<String> = emptyList(),
    val scheduleIds: List<String> = emptyList(),
    val feedingIds: List<String> = emptyList()
)
