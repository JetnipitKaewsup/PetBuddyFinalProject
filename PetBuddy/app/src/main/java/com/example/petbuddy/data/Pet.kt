package com.example.petbuddy.data

import com.google.firebase.Timestamp

data class Pet(
    var petId: String = "",
    val userId: String = "",
    val petName: String = "",
    val sex: String = "",
    val breed: String = "",
    val petTypeId: String = "",
    val birthDate: Timestamp? = null,
    val imagePath: String = "",
    var isSelected: Boolean = false,
)
