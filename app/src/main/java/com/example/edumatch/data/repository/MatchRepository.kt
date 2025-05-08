package com.example.edumatch.data.repository

import com.example.edumatch.data.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatchesForUser(userId: String): Flow<List<Match>>
    suspend fun createMatch(match: Match)
    suspend fun deleteMatch(matchId: String)
} 