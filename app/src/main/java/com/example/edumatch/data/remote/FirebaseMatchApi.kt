package com.example.edumatch.data.remote

import com.example.edumatch.data.model.Match
import kotlinx.coroutines.flow.Flow

interface FirebaseMatchApi {
    fun getMatchesForUser(userId: String): Flow<List<Match>>
    suspend fun createMatch(match: Match)
    suspend fun deleteMatch(matchId: String)
} 