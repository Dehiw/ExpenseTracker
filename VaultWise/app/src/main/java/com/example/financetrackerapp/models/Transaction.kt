package com.example.financetrackerapp.models

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(), // Unique identifier for each transaction
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val type: String // Add this field: "Income" or "Expense"
)