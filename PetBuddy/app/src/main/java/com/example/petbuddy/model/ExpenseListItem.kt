package com.example.petbuddy.model

sealed class ExpenseListItem {

    data class Header(
        val date: String
    ) : ExpenseListItem()

    data class Expense(
        val record: ExpenseRecord
    ) : ExpenseListItem()
}