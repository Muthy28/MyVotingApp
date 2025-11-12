package com.example.myvotingapp

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "candidates",
    foreignKeys = [ForeignKey(
        entity = Position::class,
        parentColumns = ["positionId"],
        childColumns = ["positionId"],
        onDelete = ForeignKey.CASCADE // Ensures candidates are removed if their position is deleted
    )],
    indices = [Index(value = ["positionId"])] // Improves query performance
)
data class Candidate(
    @PrimaryKey(autoGenerate = true)
    val candidateId: Long = 0,
    val name: String,
    val positionId: Long,      // foreign key to Position (no enforced FK here for simplicity)
    val manifesto: String,
    val imageUrl: String?,     // local drawable URI or remote URL
    val voteCount: Int = 0     // optional cached count
)
