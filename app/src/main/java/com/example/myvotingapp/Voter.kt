package com.example.myvotingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voters")
data class Voter(
    @PrimaryKey val idNumber: String, // ID is unique
    val firstName: String,
    val lastName: String,
    val mobile: String,
    val password: String // store plain for demo; in real app hash it
)
