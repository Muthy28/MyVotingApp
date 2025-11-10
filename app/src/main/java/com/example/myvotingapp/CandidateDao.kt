package com.example.myvotingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CandidateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: Candidate): Long

    @Update
    suspend fun updateCandidate(candidate: Candidate)

    @Delete
    suspend fun deleteCandidate(candidate: Candidate)

    @Query("SELECT * FROM candidates WHERE positionId = :positionId")
    fun getCandidatesForPositionFlow(positionId: Long): Flow<List<Candidate>>

    @Query("SELECT * FROM candidates")
    fun getAllCandidatesFlow(): Flow<List<Candidate>>

    @Query("SELECT * FROM candidates WHERE candidateId = :id LIMIT 1")
    suspend fun getCandidateById(id: Long): Candidate?

    @Query("UPDATE candidates SET voteCount = voteCount + 1 WHERE candidateId = :id")
    suspend fun incrementVoteCount(id: Long)
}
