package com.example.petbuddy.model

import java.util.Date

data class Todo(

    var title: String = "",
    var description: String = "",
    var startDate: Date? = null,
    var completed: Boolean = false

)