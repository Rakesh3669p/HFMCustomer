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
import com.hfm.customer.databinding.ItemBulkOrderBinding
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import javax.inject.Inject


class ToShipAdapter @Inject constructor() :
    RecyclerView.Adapter<ToShipAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBulkOrderBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
                /*if(data.product_image.isNotEmpty()) {
                    val imageOriginal = data.product_image[0].image
                    val imageReplaced =
                        imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.load(imageReplaced)
                }

                requestId.text = "Request #:${data.bulkrequest_id}"
                orderId.text = "Order #:${data.bulkrequest_order_id}"*/

                requestStatusLbl.makeGone()
                requestStatus.text = "In Process"
                val orangeColor = ContextCompat.getColor(context, R.color.orange)
                requestStatus.setTextColor(orangeColor)

            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToShipAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBulkOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ToShipAdapter.ViewHolder, position: Int) {
        holder.bind("")
    }

    override fun getItemCount(): Int = 5

    private var onCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (id: Int) -> Unit) {
        onCategoryClick = listener
    }
}