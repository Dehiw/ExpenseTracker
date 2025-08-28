package com.example.financetrackerapp.storage

import android.content.Context
import android.util.Log
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.models.Budget
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class BackupData(
    val transactions: List<Transaction>,
    val budget: Budget
)

class FileStorageManager(private val context: Context) {

    private val fileName = "finance_backup.json"
    private val gson = Gson()

    // Save Transactions and Budget to File
    fun saveBackupData(transactions: List<Transaction>, budget: Budget): Boolean {
        return try {
            val backupData = BackupData(transactions, budget)
            val jsonString = gson.toJson(backupData)
            val file = File(context.filesDir, fileName)
            val writer = FileWriter(file)
            writer.write(jsonString)
            writer.close()
            true
        } catch (e: Exception) {
            Log.e("FileStorage", "Error saving backup data: ${e.message}")
            false
        }
    }

    // Load Transactions and Budget from File
    fun loadBackupData(): BackupData {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return BackupData(emptyList(), Budget(0.0, 0.0))
            val reader = FileReader(file)
            val jsonString = reader.readText()
            reader.close()
            gson.fromJson(jsonString, BackupData::class.java)
        } catch (e: Exception) {
            Log.e("FileStorage", "Error loading backup data: ${e.message}")
            BackupData(emptyList(), Budget(0.0, 0.0))
        }
    }
}
