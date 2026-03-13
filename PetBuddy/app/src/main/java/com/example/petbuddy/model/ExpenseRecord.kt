package com.example.petbuddy.model

data class ExpenseRecord(

    val petId: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val currency: String = "THB",
    val date: String = "",
    val timestamp: Long = 0
)