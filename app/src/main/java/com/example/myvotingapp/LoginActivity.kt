package com.example.myvotingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myvotingapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dao: VoterDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = AppDatabase.getDatabase(this).voterDao()

        // Pre-fill ID if coming from registration
        binding.etIdNumber.setText(intent.getStringExtra("ID_NUMBER") ?: "")

        binding.btnLogin.setOnClickListener {
            val idNumber = binding.etIdNumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (idNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter ID and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val voter = dao.getVoterById(idNumber)

                runOnUiThread {
                    when {
                        idNumber == "12345678" && password == "admin" -> {
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        }
                        voter != null && voter.password.trim() == password -> {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("LOGGED_IN_VOTER_ID", idNumber) // Added this line
                            startActivity(intent)
                        }
                        else -> {
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}