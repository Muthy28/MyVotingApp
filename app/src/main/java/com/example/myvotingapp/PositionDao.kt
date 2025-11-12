package com.example.myvotingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(position: Position): Long

    @Update
    suspend fun update(position: Position)


    @Query("SELECT * FROM positions")
    fun getAllPositionsFlow(): Flow<List<Position>>

    @Query("SELECT * FROM positions WHERE positionId = :id LIMIT 1")
    suspend fun getPositionById(id: Long): Position?
    @Delete
    suspend fun delete(position: Position)

}
