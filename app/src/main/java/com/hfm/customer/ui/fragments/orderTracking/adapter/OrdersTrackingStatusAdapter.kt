package com.hfm.customer.ui.fragments.orderTracking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemOrderTrackBinding
import javax.inject.Inject


class OrdersTrackingStatusAdapter @Inject constructor() :
    RecyclerView.Adapter<OrdersTrackingStatusAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemOrderTrackBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
               
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String   , newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrdersTrackingStatusAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemOrderTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersTrackingStatusAdapter.ViewHolder, position: Int) {
//        holder.bind(differ.currentList[position])
        holder.bind("")
    }

    override fun getItemCount(): Int = 5

    private var onOrderClick: ((id: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (id: Int) -> Unit) {
        onOrderClick = listener
    }
}