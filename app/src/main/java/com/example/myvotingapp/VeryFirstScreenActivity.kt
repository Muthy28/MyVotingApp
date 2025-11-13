package com.example.myvotingapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myvotingapp.databinding.ActivityVeryFirstScreenBinding

class VeryFirstScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVeryFirstScreenBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "MyVotingAppPrefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_IS_ADMIN = "is_admin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is remembered before setting content view
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)

        if (isRemembered) {
            // User is remembered, redirect to appropriate activity
            val userId = sharedPreferences.getString(KEY_LOGGED_IN_USER_ID, "") ?: ""
            val isAdmin = sharedPreferences.getBoolean(KEY_IS_ADMIN, false)

            if (isAdmin) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("LOGGED_IN_VOTER_ID", userId)
                startActivity(intent)
            }
            finish()
            return
        }

        // If not remembered, show the normal first screen
        binding = ActivityVeryFirstScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}