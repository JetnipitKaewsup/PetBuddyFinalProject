package com.example.petbuddy.model

import com.google.firebase.Timestamp

data class Pet(
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val birthdate: Timestamp? = null,
    val imageUrl: String = ""
)
