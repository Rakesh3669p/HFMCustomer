package com.hfm.customer.ui.fragments.vouchers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemVoucherFullBinding
import com.hfm.customer.databinding.ItemWalletTransactionBinding
import javax.inject.Inject

class VouchersAdapter @Inject constructor() : RecyclerView.Adapter<VouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemVoucherFullBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {

            with(binding) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemVoucherFullBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind("")
    }

    override fun getItemCount(): Int = 4
}