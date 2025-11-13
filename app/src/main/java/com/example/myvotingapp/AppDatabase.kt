package com.example.myvotingapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Voter::class, Position::class, Candidate::class, Vote::class], version = 2) // ← CHANGED from 1 to 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun voterDao(): VoterDao
    abstract fun positionDao(): PositionDao
    abstract fun candidateDao(): CandidateDao
    abstract fun voteDao(): VoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_voting_app_db"
                )
                    .fallbackToDestructiveMigration() // ← ADD THIS LINE
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed data synchronously
                            INSTANCE?.let { database ->
                                CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    seedData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedData(database: AppDatabase) {
            val voterDao = database.voterDao()
            val positionDao = database.positionDao()
            val candidateDao = database.candidateDao()

            // Insert admin voter
            val admin = Voter(
                idNumber = "12345678",
                firstName = "Admin",
                lastName = "User",
                mobile = "0000000000",
                password = "admin"
            )
            voterDao.insertVoter(admin)

            // Insert positions and get their auto-generated IDs
            val presidentialPosition = Position(name = "Presidential Race")
            val governorPosition = Position(name = "Governor Race")

            val presidentialPositionId = positionDao.insert(presidentialPosition)
            val governorPositionId = positionDao.insert(governorPosition)

            // Insert presidential candidates using the actual position ID
            candidateDao.insertCandidate(Candidate(
                name = "John Smith",
                positionId = presidentialPositionId,
                manifesto = "Building a better future for all citizens with focus on education and healthcare reform.",
                imageUrl = null,
                voteCount = 0
            ))

            candidateDao.insertCandidate(Candidate(
                name = "Sarah Johnson",
                positionId = presidentialPositionId,
                manifesto = "Economic growth and environmental sustainability for our nation's prosperity.",
                imageUrl = null,
                voteCount = 0
            ))

            // Insert governor candidates using the actual position ID
            candidateDao.insertCandidate(Candidate(
                name = "Mike Brown",
                positionId = governorPositionId,
                manifesto = "Local economic development and job creation for our state's growth.",
                imageUrl = null,
                voteCount = 0
            ))

            candidateDao.insertCandidate(Candidate(
                name = "Lisa Davis",
                positionId = governorPositionId,
                manifesto = "Environmental protection and sustainable development for our communities.",
                imageUrl = null,
                voteCount = 0
            ))
        }
    }
}