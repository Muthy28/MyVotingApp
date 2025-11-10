package com.example.myvotingapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Voter::class, Position::class, Candidate::class, Vote::class], version = 1)
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed admin synchronously
                            INSTANCE?.let { database ->
                                val voterDao = database.voterDao()
                                CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    val admin = Voter(
                                        idNumber = "12345678",
                                        firstName = "Admin",
                                        lastName = "User",
                                        mobile = "0000000000",
                                        password = "admin"
                                    )
                                    voterDao.insertVoter(admin) // insert admin
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
