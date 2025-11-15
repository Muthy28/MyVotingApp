package com.example.myvotingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myvotingapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dao: VoterDao
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = AppDatabase.getDatabase(this).voterDao()

        // Setup password toggle functionality
        setupPasswordToggle()

        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val mobile = binding.etMobileNumber.text.toString().trim()
            val idNumber = binding.etIdNumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || mobile.isEmpty() || idNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val voter = Voter(firstName = firstName, lastName = lastName, mobile = mobile, idNumber = idNumber, password = password)

            lifecycleScope.launch {
                try {
                    dao.insertVoter(voter)
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        // Go to login with ID prefilled
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                            putExtra("ID_NUMBER", idNumber)
                        })
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "User already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupPasswordToggle() {
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                // Hide password
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }

            // Move cursor to end
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }
}