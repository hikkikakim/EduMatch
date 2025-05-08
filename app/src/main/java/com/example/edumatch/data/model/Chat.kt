package com.example.edumatch.data.model

data class Chat(
    val id: String = "",
    val userIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
) 