package com.example.myvotingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "positions")
data class Position(
    @PrimaryKey(autoGenerate = true) val positionId: Long = 0,
    val name: String // e.g., "Presidential", "Governor"
)
