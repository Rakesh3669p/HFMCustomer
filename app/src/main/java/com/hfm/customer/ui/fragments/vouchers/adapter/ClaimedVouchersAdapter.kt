package com.hfm.customer.ui.fragments.vouchers.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemVoucherClaimedBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
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
//                sellerImage.loadImage(data.)
                voucherExpiry.text = "Expires on: ${data.validUpto}"

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