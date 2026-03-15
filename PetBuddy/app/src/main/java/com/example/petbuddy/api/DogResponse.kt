package com.example.petbuddy.api

data class DogResponse(
    val message: Map<String, List<String>>,
    val status: String,
)