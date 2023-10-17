package com.hfm.customer.ui.fragments.myOrders.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemAdsBinding
import com.hfm.customer.databinding.ItemBulkOrderBinding
import com.hfm.customer.ui.dashBoard.home.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.Brand
import com.hfm.customer.ui.fragments.myOrders.model.BulkrequestOrderDetail
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class MyOrdersListAdapter @Inject constructor() :
    RecyclerView.Adapter<MyOrdersListAdapter.ViewHolder>() {
    private  var paymentStatus: String=""

    private lateinit var context: Context


    inner class ViewHolder(private val bind: ItemBulkOrderBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Purchase) {
            val redColor = ContextCompat.getColor(context, R.color.red)
            val greenColor = ContextCompat.getColor(context, R.color.green)
            val orangeColor = ContextCompat.getColor(context, R.color.orange)

            with(bind) {
                requestStatusLbl.makeGone()
                if(data.products.isNotEmpty()) {
                    data.products[0].let {
                        if (it.product_image!=null&&it.product_image.isNotEmpty()) {
                            productImage.load(replaceBaseUrl(it.product_image[0].image))
                        }
                    }
                }
                requestId.isVisible = false
                orderId.text = "Order #: ${data.order_id}"
                requestedDate.text = "${data.order_date} | ${data.order_time}"

                when(paymentStatus){
                    "pending"->{
                        requestStatus.text = "Payment Pending"
                        requestStatus.setTextColor(orangeColor)
                    }
                    "confirmed"->{
                        requestStatus.text = "In Process"
                        requestStatus.setTextColor(orangeColor)
                    }

                    "progress"->{
                        requestStatus.text = "Estimated delivery on "
                        requestStatus.setTextColor(greenColor)
                    }

                    "delivered"->{
                        requestStatus.text = "Delivered on ${data.delivered_date}"
                        requestStatus.setTextColor(greenColor)
                    }

                    "rejected"->{
                        requestStatus.text = "Cancelled on "
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
    ): MyOrdersListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBulkOrderBinding.inflate(
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