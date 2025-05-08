package com.example.edumatch.data.repository

import com.example.edumatch.data.model.Chat
import com.example.edumatch.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatsForUser(userId: String): Flow<List<Chat>>
    fun getMessagesForChat(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
} 