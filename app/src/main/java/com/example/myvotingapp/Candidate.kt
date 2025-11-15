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
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["positionId"])]
)
data class Candidate(
    @PrimaryKey(autoGenerate = true)
    val candidateId: Long = 0,
    val name: String,
    val positionId: Long,
    val manifesto: String,
    val imageUrl: String?,
    val voteCount: Int = 0
)
