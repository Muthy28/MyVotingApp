package com.example.myvotingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.myvotingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load HomeFragment by default
        supportFragmentManager.commit {
            replace(R.id.fragmentContainerView, HomeFragment())
        }

        // Bottom navigation setup
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.commit { replace(R.id.fragmentContainerView, HomeFragment()) }
                    true
                }
                R.id.nav_votes -> { /* TODO: Votes Fragment */ true }
                R.id.nav_my_votes -> { /* TODO: MyVotes Fragment */ true }
                R.id.nav_profile -> { /* TODO: Profile Fragment */ true }
                else -> false
            }
        }
    }
}
