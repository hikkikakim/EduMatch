package com.example.edumatch.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val age: Int = 18,
    val faculty: String = "",
    val major: String = "",
    val interests: List<String> = emptyList(),
    val description: String = "",
    val photos: List<String> = emptyList(),
) 