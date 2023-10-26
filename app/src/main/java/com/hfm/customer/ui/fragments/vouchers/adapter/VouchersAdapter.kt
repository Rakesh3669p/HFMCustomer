package com.hfm.customer.ui.fragments.vouchers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemVoucherFullBinding
import com.hfm.customer.databinding.ItemWalletTransactionBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import javax.inject.Inject

class VouchersAdapter @Inject constructor() : RecyclerView.Adapter<VouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val binding: ItemVoucherFullBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Coupon) {
            with(binding) {
                discountPercent.text = data.offer
                discountDescription.text = "Min. Spend RM${data.minimumPurchase} Capped at ${data.offerValue}"
                voucherExpiry.text = "Expires on: ${data.validUpto}"
                userNow.setOnClickListener {
                    onItemClick?.invoke(absoluteAdapterPosition)
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
            ItemVoucherFullBinding.inflate(
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