package com.example.myvotingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVote(vote: Vote): Long

    @Query("SELECT * FROM votes WHERE voterId = :voterId")
    fun getVotesForVoterFlow(voterId: String): Flow<List<Vote>>

    @Query("SELECT COUNT(*) FROM votes WHERE voterId = :voterId AND positionId = :positionId")
    suspend fun hasVotedForPosition(voterId: String, positionId: Long): Int

    @Query("DELETE FROM votes")
    suspend fun deleteAll()
}
