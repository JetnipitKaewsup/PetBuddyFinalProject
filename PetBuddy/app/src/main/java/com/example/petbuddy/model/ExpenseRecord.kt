package com.example.petbuddy.model

data class ExpenseRecord(

    var id: String = "",

    val title: String = "",
    val amount: Double = 0.0,

    val category: String = "",

    val petId: String = "",
    val petName: String = "",

    val timestamp: Long = System.currentTimeMillis()
)