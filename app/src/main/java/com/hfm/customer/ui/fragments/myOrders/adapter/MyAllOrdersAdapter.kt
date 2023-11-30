package com.hfm.customer.ui.fragments.myOrders.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemMyOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject
import kotlin.math.abs


class MyAllOrdersAdapter @Inject constructor() :
    RecyclerView.Adapter<MyAllOrdersAdapter.ViewHolder>() {

    private var from: String = ""
    private lateinit var context: Context
    var selectedPosition: Int = -1

    inner class ViewHolder(private val bind: ItemMyOrdersBinding) :
        RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: Purchase) {

            with(bind) {
                orderId.text = "Order #: ${data.order_id}"
                requestedDate.text = "${data.order_date} | ${data.order_time}"
                price.text = "RM ${formatToTwoDecimalPlaces(data.grand_total.toString().toDouble())} (${data.products.size}) items"
                val productsAdapter = MyAllOrdersProductsAdapter()
                initRecyclerView(context, productsRv, productsAdapter)
                productsAdapter.differ.submitList(data.products)

                if (selectedPosition == adapterPosition) {
                    arrow.rotation = 90f
                    productsExpand.isVisible = true
                } else {
                    arrow.rotation = 0f
                    productsExpand.isVisible = false
                }

                root.setOnClickListener {
                    val previousPosition = selectedPosition
                    selectedPosition = if (selectedPosition == absoluteAdapterPosition) {
                        -1
                    } else {
                        absoluteAdapterPosition
                    }

                    notifyItemChanged(selectedPosition)
                    notifyItemChanged(previousPosition)
                }

                select.text = if (from == "chat") {
                    "Chat"
                } else {
                    "Select"
                }

                select.setOnClickListener {
                    onOrderClick?.let {
                        it(absoluteAdapterPosition)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Purchase>() {
        override fun areItemsTheSame(
            oldItem: Purchase,
            newItem: Purchase
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Purchase,
            newItem: Purchase
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAllOrdersAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemMyOrdersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyAllOrdersAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onOrderClick: ((id: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (id: Int) -> Unit) {
        onOrderClick = listener
    }

    fun setFrom(from: String) {

        this.from = from
    }

}