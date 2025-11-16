package com.example.myvotingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "votes")
data class Vote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voterId: String,
    val candidateId: Long,
    val positionId: Long
)
