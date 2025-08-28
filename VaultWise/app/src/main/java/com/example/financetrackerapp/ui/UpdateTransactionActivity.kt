package com.example.financetrackerapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetrackerapp.R
import com.example.financetrackerapp.databinding.ActivityUpdateTransactionBinding
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.storage.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.*

class UpdateTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateTransactionBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefManager = SharedPrefManager(this)

        // Populate the category dropdown with AutoCompleteTextView
        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.spCategory.setAdapter(adapter)

        // Get transaction details from intent
        transactionId = intent.getStringExtra("transaction_id")
        if (transactionId == null) {
            Toast.makeText(this, "Transaction ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val transaction = sharedPrefManager.getTransactionById(transactionId!!)
        if (transaction != null) {
            // Pre-fill the form with transaction details
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            binding.etDate.setText(transaction.date)

            // Set the category selection
            binding.spCategory.setText(transaction.category, false)
            
            // Pre-select the transaction type
            when (transaction.type) {
                "Income" -> binding.radioIncome.isChecked = true
                "Expense" -> binding.radioExpense.isChecked = true
            }
        }

        // Set up date picker for the date field
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.etDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Handle update button click
        binding.btnUpdate.setOnClickListener {
            updateTransaction()
        }
        
        // Handle cancel button click
        binding.btnCancel.setOnClickListener {
            finish() // Go back to previous screen
        }
    }

    private fun updateTransaction() {
        val title = binding.etTitle.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val amount = amountText.toDoubleOrNull()
        val date = binding.etDate.text.toString()
        val category = binding.spCategory.text.toString() // Get from AutoCompleteTextView text
        
        // Get the selected transaction type
        val type = when {
            binding.radioIncome.isChecked -> "Income"
            binding.radioExpense.isChecked -> "Expense"
            else -> ""
        }

        // Validate inputs
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Amount must be a positive number", Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        if (type.isEmpty()) {
            Toast.makeText(this, "Please select a transaction type", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated transaction
        val updatedTransaction = Transaction(
            id = transactionId!!,
            title = title,
            amount = amount,
            category = category,
            date = date,
            type = type
        )

        // Update transaction in SharedPreferences
        val success = sharedPrefManager.updateTransaction(updatedTransaction)
        if (success) {
            Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        } else {
            Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show()
        }
    }
}