package com.example.petbuddy.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Pet(

    var petId: String = "",
    val petName: String = "",
    val sex: String = "",
    val breed: String = "",
    val petType: String = "",
    val birthDate: Timestamp? = null,
    val imageUrl: String? = null

) : Serializable