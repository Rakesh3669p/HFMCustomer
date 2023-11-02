package com.hfm.customer.ui.fragments.support.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemSupportBinding
import com.hfm.customer.ui.fragments.support.model.SupportTickets
import javax.inject.Inject

class SupportAdapter @Inject constructor() : RecyclerView.Adapter<SupportAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemSupportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data:SupportTickets) {

            with(binding) {
                ticketId.text = "#${data.ticket_id}"
                dateLbl.text = data.created_at
                titleLbl.text = data.subject
                desc.text = data.last_message

                root.setOnClickListener {
                    onItemClick?.invoke(data.support_id)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<SupportTickets>() {
        override fun areItemsTheSame(oldItem: SupportTickets, newItem: SupportTickets): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SupportTickets, newItem: SupportTickets): Boolean {
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
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }
}