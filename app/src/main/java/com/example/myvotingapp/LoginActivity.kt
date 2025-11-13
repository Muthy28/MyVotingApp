package com.example.myvotingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myvotingapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dao: VoterDao

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "LoginActivity created")

        try {
            dao = AppDatabase.getDatabase(this).voterDao()
            Log.d(TAG, "Database initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database: ${e.message}", e)
            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Pre-fill ID if coming from registration
        binding.etIdNumber.setText(intent.getStringExtra("ID_NUMBER") ?: "")

        binding.btnLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")

            val idNumber = binding.etIdNumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (idNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter ID and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login for ID: $idNumber")

            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting database query for voter: $idNumber")
                    val voter = dao.getVoterById(idNumber)
                    Log.d(TAG, "Database query completed. Voter found: ${voter != null}")

                    runOnUiThread {
                        Log.d(TAG, "Processing login result")
                        when {
                            idNumber == "12345678" && password == "admin" -> {
                                Log.d(TAG, "Admin login successful")
                                startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                                finish()
                            }
                            voter != null && voter.password.trim() == password -> {
                                Log.d(TAG, "Voter login successful for: ${voter.firstName}")
                                try {
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.putExtra("LOGGED_IN_VOTER_ID", idNumber)
                                    Log.d(TAG, "Starting MainActivity...")
                                    startActivity(intent)
                                    finish()
                                    Log.d(TAG, "MainActivity started successfully")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error starting MainActivity: ${e.message}", e)
                                    Toast.makeText(this@LoginActivity, "Error starting app: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            else -> {
                                Log.d(TAG, "Invalid credentials")
                                Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during login process: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            Log.d(TAG, "Navigate to RegisterActivity")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}