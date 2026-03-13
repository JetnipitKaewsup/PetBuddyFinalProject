package com.example.petbuddy.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val profileImage: String? = null,
    val createAccount: Boolean = false
)
