package com.hfm.customer.ui.fragments.myOrders.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemBulkOrderBinding
import com.hfm.customer.ui.fragments.myOrders.model.BulkrequestOrderDetail
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class BulkOrdersAdapter @Inject constructor() :
    RecyclerView.Adapter<BulkOrdersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBulkOrderBinding) :
        RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: BulkrequestOrderDetail) {
            with(bind) {
                if(data.product_image.isNotEmpty()) {
                    productImage.loadImage(replaceBaseUrl(data.product_image[0].image))
                }

                requestId.text = "Request #:${data.bulkrequest_order_id}"
                orderId.isVisible = data.order_id.isNotEmpty()
                orderId.text = "Order #:${data.order_id}"
                orderAmount.isVisible = false

                requestStatusLbl.isVisible =true
                requestStatus.text = if(data.request_status==0) "Pending" else "Accepted"
                val orangeColor = ContextCompat.getColor(context,R.color.orange)
                val greenColor = ContextCompat.getColor(context,R.color.green)
                if(data.request_status==0)  {
                    requestStatus.setTextColor(orangeColor)
                }else{
                    requestStatus.setTextColor(greenColor)
                }
                requestedDate.text = "${data.date_needed} | ${data.request_time}"

                root.setOnClickListener {
                    onOrderClick?.invoke(absoluteAdapterPosition)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<BulkrequestOrderDetail>() {
        override fun areItemsTheSame(oldItem: BulkrequestOrderDetail, newItem: BulkrequestOrderDetail): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: BulkrequestOrderDetail   , newItem: BulkrequestOrderDetail): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BulkOrdersAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBulkOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BulkOrdersAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onOrderClick: ((position: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (position:Int) -> Unit) {
        onOrderClick = listener
    }
}