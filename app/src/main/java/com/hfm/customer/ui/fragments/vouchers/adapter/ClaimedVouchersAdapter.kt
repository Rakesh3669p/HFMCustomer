package com.hfm.customer.ui.fragments.vouchers.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemVoucherClaimedBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject

class ClaimedVouchersAdapter @Inject constructor() : RecyclerView.Adapter<ClaimedVouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemVoucherClaimedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: Coupon) {
            with(binding) {
                discountPercent.text = data.title
                discountDescription.text = data.desc
                sellerImage.loadImage(replaceBaseUrl(data.storeLogo))
//                voucherExpiry.text = "Expires on: ${data.validUpto}"

                val red = ContextCompat.getColorStateList(context, R.color.red)
                val grey = ContextCompat.getColorStateList(context, R.color.textGreyDark)
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
                        useNow.text = "Claimed"
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
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemVoucherClaimedBinding.inflate(
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

    private var onItemClick: ((position: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (position: Int) -> Unit) {
        onItemClick = listener
    }
}