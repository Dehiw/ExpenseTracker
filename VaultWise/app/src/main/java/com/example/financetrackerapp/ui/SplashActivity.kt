package com.example.financetrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.financetrackerapp.R

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DURATION = 1500L // 1.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnboardingScreen1::class.java))
            finish() // Close this activity so it's not in the back stack
        }, SPLASH_DURATION)
    }
}