package com.example.financetrackerapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.models.Budget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SharedPrefManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("FinanceAppPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    // Save Single Transaction Without Overwriting
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        editor.putString("transactions", json)
        editor.apply()
    }

    /**
     * Add a new transaction to the existing list
     */
    fun addTransaction(transaction: Transaction): Boolean {
        val currentTransactions = getTransactions().toMutableList()
        currentTransactions.add(transaction)
        saveTransactions(currentTransactions)
        return true
    }

    /**
     * Update an existing transaction by ID
     * @return true if transaction was found and updated, false otherwise
     */
    fun updateTransaction(updatedTransaction: Transaction): Boolean {
        val currentTransactions = getTransactions().toMutableList()
        val index = currentTransactions.indexOfFirst { it.id == updatedTransaction.id }

        if (index != -1) {
            currentTransactions[index] = updatedTransaction
            saveTransactions(currentTransactions)
            return true
        }
        return false
    }

    /**
     * Delete a transaction by ID
     * @return true if transaction was found and deleted, false otherwise
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteTransaction(transactionId: String): Boolean {
        val currentTransactions = getTransactions().toMutableList()
        val initialSize = currentTransactions.size

        currentTransactions.removeIf { it.id == transactionId }

        if (currentTransactions.size < initialSize) {
            saveTransactions(currentTransactions)
            return true
        }
        return false
    }

    /**
     * Delete multiple transactions by ID
     * @return the number of transactions deleted
     */

    /**
     * Get a specific transaction by ID
     * @return the transaction or null if not found
     */
    fun getTransactionById(transactionId: String): Transaction? {
        return getTransactions().find { it.id == transactionId }
    }

    // Retrieve Transactions List
    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString("transactions", null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveBudget(budget: Budget) {
        val json = gson.toJson(budget)
        editor.putString("budget", json)
        editor.apply()
    }

    fun getBudget(): Budget {
        val json = sharedPreferences.getString("budget", null)
        return if (json != null) {
            gson.fromJson(json, Budget::class.java)
        } else {
            Budget(0.0, 0.0) // Default: $0 budget, $0 spent
        }
    }

    // Currency Preference Methods

    /**
     * Save the currency symbol preference (e.g., $, €, £)
     */
    fun saveCurrencySymbol(symbol: String) {
        editor.putString("currency_symbol", symbol)
        editor.apply()
    }

    /**
     * Get the currency symbol, default is $ if not set
     */
    fun getCurrencySymbol(): String {
        return sharedPreferences.getString("currency_symbol", "$") ?: "$"
    }

    /**
     * Save the currency code (e.g., USD, EUR, GBP)
     */
    fun saveCurrencyCode(code: String) {
        editor.putString("currency_code", code)
        editor.apply()
    }

    /**
     * Get the currency code, default is USD if not set
     */
    fun getCurrencyCode(): String {
        return sharedPreferences.getString("currency_code", "USD") ?: "USD"
    }

    /**
     * Save both currency symbol and code at once
     */
    fun saveCurrency(symbol: String, code: String) {
        editor.putString("currency_symbol", symbol)
        editor.putString("currency_code", code)
        editor.apply()
    }

    /**
     * Format an amount with the current currency symbol
     */
    fun formatCurrency(amount: Double): String {
        val symbol = getCurrencySymbol()
        return "$symbol${String.format("%.2f", amount)}"
    }

    fun clearAllData() {
        editor.clear() // Clears all saved data
        editor.apply()
    }
}