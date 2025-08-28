package com.example.financetrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetrackerapp.R
import com.example.financetrackerapp.adapters.TransactionAdapter
import com.example.financetrackerapp.databinding.ActivityTransactionsBinding
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.storage.SharedPrefManager

class TransactionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        sharedPrefManager = SharedPrefManager(this)
        setupRecyclerView()
    }

    private fun setupToolbar() {
        // Handle back button click
        binding.btnBack.setOnClickListener {
            // Navigate to MainActivity (home)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        val transactions = sharedPrefManager.getTransactions().toMutableList()
        val sortedTransactions = transactions.sortedByDescending { it.date }.toMutableList()

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(this@TransactionsActivity)
            setHasFixedSize(true)
            adapter = TransactionAdapter(
                transactions = sortedTransactions,
                sharedPrefManager = sharedPrefManager,
                onUpdateClick = { transaction: Transaction ->
                    Toast.makeText(
                        this@TransactionsActivity,
                        "Update clicked for ${transaction.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onTransactionDeleted = {
                    setupRecyclerView() // Refresh the list
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView() // Refresh the list when returning to this activity
    }
} 