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
import com.hfm.customer.databinding.ItemOrderListBinding
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class MyOrdersListAdapter @Inject constructor() :
    RecyclerView.Adapter<MyOrdersListAdapter.ViewHolder>() {
    private  var paymentStatus: String=""

    private lateinit var context: Context


    inner class ViewHolder(private val bind: ItemOrderListBinding) :
        RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: Purchase) {
            val redColor = ContextCompat.getColor(context, R.color.red)
            val greenColor = ContextCompat.getColor(context, R.color.green)
            val orangeColor = ContextCompat.getColor(context, R.color.orange)

            with(bind) {
                requestStatusLbl.makeGone()
                if(data.products.isNotEmpty()) {
                    data.products[0].let {
                        if (!it.product_image.isNullOrEmpty()) {
                            productImage.loadImage(replaceBaseUrl(it.product_image[0].image))
                        }
                    }
                }
                requestId.isVisible = false
                orderId.text = "Order #: ${data.order_id}"

                requestedDate.text = "${data.order_date} | ${data.order_time}"
                orderAmount.text = "RM ${formatToTwoDecimalPlaces(data.grand_total)} (${data.products.size} Items)"

                when(paymentStatus){
                    "to_pay"->{
                        requestStatus.text = data.frontend_order_status
                        requestStatus.setTextColor(orangeColor)
                    }
                    "to_ship"->{
                        requestStatus.text = data.frontend_order_status
                        requestStatus.setTextColor(orangeColor)
                    }

                    "to_receive"->{
                        requestStatus.text = data.frontend_order_status
                        requestStatus.setTextColor(greenColor)
                    }

                    "completed"->{
                        requestStatus.text = data.frontend_order_status
                        requestStatus.setTextColor(greenColor)
                    }

                    "cancelled"->{
                        requestStatus.text = data.frontend_order_status
                        requestStatus.setTextColor(redColor)
                    }
                }

                root.setOnClickListener {
                    onOrderClick?.invoke(data)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Purchase>() {
        override fun areItemsTheSame(
            oldItem: Purchase,
            newItem: Purchase
        ): Boolean =  oldItem == newItem

        override fun areContentsTheSame(
            oldItem: Purchase,
            newItem: Purchase
        ): Boolean =oldItem == newItem
    }

    val differ:AsyncListDiffer<Purchase> = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyOrdersListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemOrderListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyOrdersListAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onOrderClick: ((data: Purchase) -> Unit)? = null

    fun setOnOrderClickListener(listener: (data: Purchase) -> Unit) {
        onOrderClick = listener
    }

    fun setPaymentStatus(status:String){
        paymentStatus= status
    }
}