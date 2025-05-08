package com.example.edumatch.data.repository

import com.example.edumatch.data.model.AllowedDomain
import kotlinx.coroutines.flow.Flow

interface AllowedDomainRepository {
    fun getAllowedDomains(): Flow<List<AllowedDomain>>
    suspend fun isDomainAllowed(domain: String): Boolean
} 