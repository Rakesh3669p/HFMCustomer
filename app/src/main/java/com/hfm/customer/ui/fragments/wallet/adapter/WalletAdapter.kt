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
import com.hfm.customer.ui.fragments.wallet.model.Wallet
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import javax.inject.Inject

class WalletAdapter @Inject constructor() : RecyclerView.Adapter<WalletAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemWalletTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Wallet) {

            with(binding) {

                if(data.credit_value){
                    transactionType.text = "Received (${data.source})"
                    transactionType.setTextColor(ContextCompat.getColor(context, R.color.green))
                    amount.setTextColor(ContextCompat.getColor(context, R.color.green))
                    amount.text = "RM ${formatToTwoDecimalPlaces(data.credit.toDouble()/100)}"
                }else{
                    transactionType.text = "Paid (${data.source})"
                    transactionType.setTextColor(ContextCompat.getColor(context, R.color.red))
                    amount.setTextColor(ContextCompat.getColor(context, R.color.red))
                    amount.text = "RM ${formatToTwoDecimalPlaces(data.debit.toDouble()/100)}"
                }
                dateLbl.text = "${data.source_ids} ${data.created_at}"


            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
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

    override fun getItemCount(): Int = differ.currentList.size
}