package com.example.financetrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.financetrackerapp.R

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val fullNameInput = findViewById<EditText>(R.id.etFullName)
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val phoneInput = findViewById<EditText>(R.id.etPhone)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)
        val termsCheckbox = findViewById<CheckBox>(R.id.cbTerms)
        val signInText = findViewById<TextView>(R.id.tvSignIn)

        // Add sign in navigation
        signInText.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            // Optional: finish this activity if you don't want it in the back stack
            // finish()
        }

        signUpButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInput(fullName, email, phone, password, confirmPassword, termsCheckbox)) {
                Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish() // Close SignUpActivity after navigating
            }
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        termsCheckbox: CheckBox
    ): Boolean {
        if (fullName.isEmpty() || fullName.length < 3) {
            Toast.makeText(this, "Full Name must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, "Invalid phone number (must be 10 digits)", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6 || !password.any { it.isDigit() } || !password.any { it.isLetter() }) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters and include letters & numbers",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!termsCheckbox.isChecked) {
            Toast.makeText(this, "You must accept the Terms & Conditions", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
