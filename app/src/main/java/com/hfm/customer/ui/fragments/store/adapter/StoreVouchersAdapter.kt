package com.hfm.customer.ui.fragments.store.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemVoucherBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import javax.inject.Inject


class StoreVouchersAdapter @Inject constructor() :
    RecyclerView.Adapter<StoreVouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemVoucherBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Coupon) {
            with(bind) {
                val red = ContextCompat.getColorStateList(context, R.color.red)
                val grey = ContextCompat.getColorStateList(context, R.color.textGreyDark)
                discountPercent.text = data.title
                discountDescription.text = data.desc

                useNow.text = if (data.isClaimed == "1") "Claimed" else "Claim Now"

                when (data.status) {
                    "not_available" -> {
                        useNow.backgroundTintList = grey
                        useNow.text = "Not Available"
                        useNow.isEnabled = false
                        discountPercent.setTextColor(grey)
                        discountDescription.setTextColor(grey)
                        voucherExpiry.setTextColor(grey)
                        voucherExpiry.text = "Expired on: ${data.validUpto}"
                    }

                    "available" -> {
                        useNow.backgroundTintList = red
                        useNow.text = if (data.isClaimed == "1") "Claimed" else "Claim Now"
                        useNow.isEnabled = true
                        discountPercent.setTextColor(red)
                        discountDescription.setTextColor(red)
                        voucherExpiry.setTextColor(red)
                        voucherExpiry.text = "Expires on: ${data.validUpto}"
                    }

                    "used" -> {
                        useNow.backgroundTintList = grey
                        useNow.text = "Used"
                        useNow.isEnabled = false
                        discountPercent.setTextColor(grey)
                        discountDescription.setTextColor(grey)
                        voucherExpiry.setTextColor(grey)
                        voucherExpiry.text = "Used on: ${data.usedOn}"
                    }

                    "expired" -> {
                        useNow.backgroundTintList = grey
                        useNow.text = "Expired"
                        useNow.isEnabled = false
                        discountPercent.setTextColor(grey)
                        discountDescription.setTextColor(grey)
                        voucherExpiry.setTextColor(grey)
                        voucherExpiry.text = "Expired on: ${data.validUpto}"
                    }
                }

                useNow.setOnClickListener {
                    if (data.isClaimed == "0") {
                        onItemClick?.invoke(absoluteAdapterPosition)
                    } else {
                        Toast.makeText(context, "Already claimed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreVouchersAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoreVouchersAdapter.ViewHolder, position: Int) {

        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }

}