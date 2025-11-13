package com.example.myvotingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVoter(voter: Voter)

    @Query("SELECT * FROM voters WHERE idNumber = :id LIMIT 1")
    suspend fun getVoterById(id: String): Voter?

    @Query("SELECT * FROM voters")
    fun getAllVotersFlow(): Flow<List<Voter>>

    @Delete
    suspend fun deleteVoter(voter: Voter)

    // ADD THIS UPDATE METHOD
    @Update
    suspend fun updateVoter(voter: Voter)
}