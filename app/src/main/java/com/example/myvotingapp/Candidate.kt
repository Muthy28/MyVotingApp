package com.example.myvotingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "candidates")
data class Candidate(
    @PrimaryKey(autoGenerate = true) val candidateId: Long = 0,
    val name: String,
    val positionId: Long,      // foreign key to Position (no enforced FK here for simplicity)
    val manifesto: String,
    val imageUrl: String?,     // local drawable URI or remote URL
    val voteCount: Int = 0     // optional cached count
)
