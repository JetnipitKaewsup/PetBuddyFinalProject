package com.example.petbuddy.data

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val username: String = "",
    val profileImage: String = "",
    val createAccount: Boolean = false,
)
