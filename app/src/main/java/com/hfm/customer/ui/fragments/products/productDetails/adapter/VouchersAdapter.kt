package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemVoucherBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import javax.inject.Inject


class VouchersAdapter @Inject constructor() :
    RecyclerView.Adapter<VouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemVoucherBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Coupon) {
            with(bind) {
                discountPercent.text = data.offer
                discountDescription.text =
                    "Min. Spend RM${data.minimumPurchase} Capped at ${data.offerValue}"
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
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VouchersAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VouchersAdapter.ViewHolder, position: Int) {

        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }

}