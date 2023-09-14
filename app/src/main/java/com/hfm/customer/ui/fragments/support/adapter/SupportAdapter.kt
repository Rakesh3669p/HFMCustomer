package com.hfm.customer.ui.fragments.vouchers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemSupportBinding
import com.hfm.customer.databinding.ItemVoucherFullBinding
import javax.inject.Inject

class SupportAdapter @Inject constructor() : RecyclerView.Adapter<SupportAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemSupportBinding) :
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
            ItemSupportBinding.inflate(
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