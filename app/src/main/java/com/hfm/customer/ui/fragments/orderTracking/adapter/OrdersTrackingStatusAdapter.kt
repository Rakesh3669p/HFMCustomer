package com.hfm.customer.ui.fragments.orderTracking.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemOrderTrackBinding
import com.hfm.customer.ui.fragments.orderTracking.Event
import javax.inject.Inject


class OrdersTrackingStatusAdapter @Inject constructor() :
    RecyclerView.Adapter<OrdersTrackingStatusAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var shippingMethod = ""
    inner class ViewHolder(private val bind: ItemOrderTrackBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Event) {
            with(bind) {
                firstLine.isVisible = absoluteAdapterPosition != differ.currentList.size-1
                viewOrderProof.isVisible = absoluteAdapterPosition == differ.currentList.size-1

                if(shippingMethod=="citylink_shipping"){
                    orderPlacedLbl.text= data.location
                    date.text = "${data.detDate} ${data.detTime}"

                }else if(shippingMethod=="dhl_shipping"){
                    orderPlacedLbl.text= data.description
                    date.text = "${data.date} ${data.time}"
                }

                viewOrderProof.setOnClickListener { onViewOrderClick?.invoke() }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Event   , newItem: Event): Boolean {
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
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onViewOrderClick: (() -> Unit)? = null

    fun setOnViewOrderClickListener(listener: () -> Unit) {
        onViewOrderClick = listener
    }

    fun setShippingMethod(shippingType:String){
        shippingMethod = shippingType
    }
}