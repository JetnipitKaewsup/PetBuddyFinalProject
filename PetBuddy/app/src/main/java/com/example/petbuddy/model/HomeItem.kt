package com.example.petbuddy.model

sealed class HomeItem {

    data class DateHeader(
        val date: String
    ) : HomeItem()

    data class TodoSection(
        val todos: List<String>
    ) : HomeItem()

    data class FeedingSection(
        val feeding: List<FeedingSchedule>,
        val petMap: Map<String, Pet>
    ) : HomeItem()

    data class UpcomingSection(
        val upcoming: List<UpcomingActivity>
    ) : HomeItem()

    data class ExpenseSection(
        val amount: Double
    ) : HomeItem()
}