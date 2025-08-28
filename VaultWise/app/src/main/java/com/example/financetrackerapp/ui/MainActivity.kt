package com.example.financetrackerapp.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetrackerapp.R
import com.example.financetrackerapp.adapters.TransactionAdapter
import com.example.financetrackerapp.databinding.ActivityMainBinding
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.storage.SharedPrefManager
// Remove pie chart imports
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.XAxis

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationHelper: NotificationHelper

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefManager = SharedPrefManager(this)
        notificationHelper = NotificationHelper(this)

        // Set LKR as the default currency
        sharedPrefManager.saveCurrencySymbol("Rs.")
        sharedPrefManager.saveCurrencyCode("LKR")

        updateBudgetUI()
        setupRecyclerView()
        setupBarChart() // Change from setupPieChart to setupBarChart
        setupBottomNavigation()
        setupFAB()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.home

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    true
                }
                R.id.transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    // Don't finish() here as we want to keep MainActivity in the back stack
                    true
                }
                R.id.manage_budget -> {
                    startActivity(Intent(this, BudgetManagementActivity::class.java))
                    true
                }
                R.id.backup -> {
                    startActivity(Intent(this, BackupRestoreActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupBarChart() {
        val transactions = sharedPrefManager.getTransactions()
        
        // Only consider expense transactions for the bar chart
        val expenseTransactions = transactions.filter { it.type == "Expense" }
        
        if (expenseTransactions.isEmpty()) {
            binding.barChart.clear()
            return
        }
        
        // Group expenses by category and sum their amounts
        val categoryMap = mutableMapOf<String, Float>()
        expenseTransactions.forEach { transaction ->
            categoryMap[transaction.category] = categoryMap.getOrDefault(transaction.category, 0f) + transaction.amount.toFloat()
        }
        
        // Create Bar Entries
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        categoryMap.entries.forEachIndexed { index, entry ->
            entries.add(BarEntry(index.toFloat(), entry.value))
            labels.add(entry.key)
        }
        
        val barDataSet = BarDataSet(entries, "Expense Categories")
        barDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        barDataSet.valueTextSize = 12f
        
        val barData = BarData(barDataSet)
        
        binding.barChart.data = barData
        binding.barChart.description.isEnabled = false
        
        // Format the X-axis to display category names
        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)
        
        // Set Y-axis properties
        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.axisRight.isEnabled = false
        
        // Animate and refresh
        binding.barChart.animateY(1000)
        binding.barChart.invalidate() // Refresh the chart
    }

    private fun updateBudgetUI() {
        val budget = sharedPrefManager.getBudget()
        val transactions = sharedPrefManager.getTransactions()

        val spentAmount = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }

        // Use your stored currency symbol
        val currencySymbol = sharedPrefManager.getCurrencySymbol()

        binding.tvTotalBudgetValue.text = "$currencySymbol${budget.totalBudget}"
        binding.tvSpentValue.text = "$currencySymbol$spentAmount"
        val remainBudget = budget.totalBudget - spentAmount
        binding.tvRemainingValue.text = "$currencySymbol$remainBudget"

        sharedPrefManager.saveBudget(budget.copy(spentAmount = spentAmount))
        checkBudgetThreshold(budget.totalBudget, spentAmount)
        setupRecyclerView()
    }

    private fun checkBudgetThreshold(totalBudget: Double, spentAmount: Double) {
        // Don't show notification if budget is zero or very small (meaning no real budget is set)
        if (totalBudget <= 0.01) {
            return
        }

        val threshold = totalBudget * 0.85
        when {
            spentAmount >= totalBudget -> {
                notificationHelper.showBudgetAlertNotification("⚠️ Budget Exceeded! You've spent $spentAmount out of $totalBudget.")
            }
            spentAmount >= threshold -> {
                notificationHelper.showBudgetAlertNotification("⚠️ Warning: You're nearing your budget limit! Spent: $spentAmount / $totalBudget.")
            }
        }
    }

    private fun setupRecyclerView() {
        val transactions = sharedPrefManager.getTransactions().toMutableList()
        val sortedTransactions = transactions.sortedByDescending { it.date }.toMutableList()

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.setHasFixedSize(true)
        binding.rvTransactions.adapter = TransactionAdapter(
            transactions = sortedTransactions,
            sharedPrefManager = sharedPrefManager,
            onUpdateClick = { transaction: Transaction ->
                Toast.makeText(this, "Update clicked for ${transaction.title}", Toast.LENGTH_SHORT).show()
            },
            onTransactionDeleted = {
                // Refresh the UI when a transaction is deleted
                updateBudgetUI()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setupBarChart() // Change from setupPieChart to setupBarChart
                }
            }
        )
    }

    private fun storeEncryptedClientName() {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Generate a secret key (store it securely in production)
        val secretKey = EncryptionUtils.generateKey()
        val keyString = EncryptionUtils.keyToString(secretKey)

        // Encrypt the client's name
        val clientName = "Your Client's Name"
        val encryptedName = EncryptionUtils.encrypt(clientName, secretKey)

        // Store the encrypted name and key in SharedPreferences
        editor.putString("encrypted_client_name", encryptedName)
        editor.putString("encryption_key", keyString)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        // Set home as selected when returning to MainActivity
        binding.bottomNavigationView.selectedItemId = R.id.home
        updateBudgetUI()
        setupBarChart()
    }

    private fun setupFAB() {
        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }
}