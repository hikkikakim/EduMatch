package com.example.edumatch.data.repository

import com.example.edumatch.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun getUserById(id: String): User?
    fun getUsersFiltered(faculty: String?, major: String?, interests: List<String>?): Flow<List<User>>
} 