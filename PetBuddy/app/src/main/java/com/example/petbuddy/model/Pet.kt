package com.example.petbuddy.model

import com.google.firebase.Timestamp

data class Pet(
    var petId: String = "",
    val petName: String = "",
    val sex: String = "",
    val breed: String = "",
    val petType: String = "",
    val birthDate: Timestamp? = null,
    val imagePath: String ?= null,
    )
