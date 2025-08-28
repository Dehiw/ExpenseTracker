package com.example.financetrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.financetrackerapp.databinding.ActivityBudgetManagementBinding
import com.example.financetrackerapp.models.Budget
import com.example.financetrackerapp.storage.SharedPrefManager

class BudgetManagementActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var binding: ActivityBudgetManagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefManager = SharedPrefManager(this)
        setupBackNavigation()
        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.btnSaveBudget.setOnClickListener {
            val budgetAmount = binding.etSetBudget.text.toString().toDoubleOrNull()
            if (budgetAmount == null || budgetAmount <= 0) {
                Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the budget
            val currentSpent = sharedPrefManager.getBudget().spentAmount
            val budget = Budget(budgetAmount, currentSpent)
            sharedPrefManager.saveBudget(budget)

            Toast.makeText(this, "Budget Updated", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
