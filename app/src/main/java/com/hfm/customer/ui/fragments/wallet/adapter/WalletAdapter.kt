package com.hfm.customer.ui.fragments.wallet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemWalletTransactionBinding
import com.hfm.customer.ui.fragments.vouchers.adapter.VouchersAdapter
import javax.inject.Inject

class WalletAdapter @Inject constructor() : RecyclerView.Adapter<WalletAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemWalletTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {

            with(binding) {
                if (adapterPosition % 2 == 0) {
                    transactionType.text = "Paid"
                    transactionType.setTextColor(ContextCompat.getColor(context, R.color.red))
                } else {
                    transactionType.text = "Received"
                    transactionType.setTextColor(ContextCompat.getColor(context, R.color.green))
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemWalletTransactionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WalletAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 4
}