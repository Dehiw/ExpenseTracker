package com.example.financetrackerapp.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapp.R
import com.example.financetrackerapp.databinding.ItemTransactionBinding
import com.example.financetrackerapp.models.Transaction
import com.example.financetrackerapp.storage.SharedPrefManager
import com.example.financetrackerapp.ui.UpdateTransactionActivity

class TransactionAdapter(
    private var transactions: MutableList<Transaction>, // Use MutableList for easier updates
    private val sharedPrefManager: SharedPrefManager, // Pass SharedPrefManager instance
    private val onUpdateClick: (Transaction) -> Unit, // Callback for update action
    private val onTransactionDeleted: () -> Unit // Callback for when transaction is deleted
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            
            val currencySymbol = sharedPrefManager.getCurrencySymbol() ?: "$"
            val formattedAmount = "$currencySymbol${transaction.amount}"
            
            binding.tvAmount.text = if (transaction.type == "Income") "+$formattedAmount" else "-$formattedAmount"
            binding.tvAmount.setTextColor(
                if (transaction.type == "Income") 
                    ContextCompat.getColor(binding.root.context, R.color.income_green)
                else 
                    ContextCompat.getColor(binding.root.context, R.color.expense_red)
            )
            
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = transaction.date
            
            // Configure the type badge based on transaction type
            val isIncome = transaction.type == "Income"
            binding.tvType.text = if (isIncome) "INC" else "EXP"
            binding.tvType.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isIncome) R.color.income_green else R.color.expense_red
                )
            )
            
            // Setup delete button listener
            binding.btnDelete.setOnClickListener {
                deleteTransaction(transaction, adapterPosition)
            }

            // Handle Update Button Click
            binding.btnUpdate.setOnClickListener {
                val intent = Intent(binding.root.context, UpdateTransactionActivity::class.java)
                intent.putExtra("transaction_id", transaction.id)
                binding.root.context.startActivity(intent)
            }
        }

        private fun deleteTransaction(transaction: Transaction, position: Int) {
            val success = sharedPrefManager.deleteTransaction(transaction.id)
            if (success) {
                transactions.removeAt(position) // Remove from the list
                notifyItemRemoved(position) // Notify RecyclerView
                onTransactionDeleted() // Call the callback to refresh the activity
                Toast.makeText(
                    binding.root.context,
                    "Transaction deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Failed to delete transaction",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        
        // Set the background color of the top area based on transaction type
        val topBackground = holder.itemView.findViewById<View>(R.id.transactionTypeBackground)
        if (transaction.type == "Income") {
            topBackground.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.income_green))
        } else {
            topBackground.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.expense_red))
        }
        
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    /**
     * Update the list of transactions and refresh the RecyclerView.
     */
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}
