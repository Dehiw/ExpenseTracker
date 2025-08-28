package com.example.financetrackerapp.ui

import android.os.Bundle
import android.app.AlertDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetrackerapp.databinding.ActivityBackupRestoreBinding
import com.example.financetrackerapp.storage.FileStorageManager
import com.example.financetrackerapp.storage.SharedPrefManager

class BackupRestoreActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var fileStorageManager: FileStorageManager
    private lateinit var binding: ActivityBackupRestoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefManager = SharedPrefManager(this)
        fileStorageManager = FileStorageManager(this)

        binding.btnBackupData.setOnClickListener {
            backupData()
        }

        binding.btnRestoreData.setOnClickListener {
            restoreData()
        }

        binding.btnClearAllData.setOnClickListener {
            confirmAndClearData()
        }
    }

    private fun backupData(): Boolean {
        val transactions = sharedPrefManager.getTransactions()
        val budget = sharedPrefManager.getBudget()
        return if (fileStorageManager.saveBackupData(transactions, budget)) {
            Toast.makeText(this, "Backup Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(this, "Backup Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun restoreData() {
        val backupData = fileStorageManager.loadBackupData()
        sharedPrefManager.saveTransactions(backupData.transactions)
        sharedPrefManager.saveBudget(backupData.budget)
        Toast.makeText(this, "Data Restored", Toast.LENGTH_SHORT).show()
    }

    private fun confirmAndClearData() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Do you want to backup your data before clearing everything?")
            .setPositiveButton("Yes, Backup & Delete") { _, _ ->
                if (backupData()) {
                    clearAllData()
                }
            }
            .setNegativeButton("No, Just Delete") { _, _ ->
                clearAllData()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        sharedPrefManager.clearAllData()
        Toast.makeText(this, "All Data Cleared", Toast.LENGTH_SHORT).show()
    }
}
