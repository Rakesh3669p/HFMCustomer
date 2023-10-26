package com.hfm.customer.ui.fragments.cart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemCartListBinding
import com.hfm.customer.databinding.ItemChatBinding
import com.hfm.customer.databinding.ItemChatUsersBinding
import com.hfm.customer.databinding.ItemPlatformVoucherBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModel
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import com.hfm.customer.ui.fragments.products.productList.adapter.ProductListAdapter
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject


class PlatformVoucherAdapter @Inject constructor() : RecyclerView.Adapter<PlatformVoucherAdapter.ViewHolder>() {
    private lateinit var context: Context
    @Inject lateinit var productsAdapter:CartProductAdapter
    private var selectedPosition = -1
    inner class ViewHolder(private val bind: ItemPlatformVoucherBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Coupon) {
            with(bind) {

                discountPercent.text = data.offer
                discountDescription.text = "Min. Spend RM${data.minimumPurchase} Capped at ${data.offerValue}"
                voucherExpiry.text = "Expires on: ${data.validUpto}"

                radioBtn.isChecked = selectedPosition == absoluteAdapterPosition

                if(absoluteAdapterPosition == differ.currentList.size-1){
                    selectedPosition = -1
                }

                radioBtn.setOnClickListener {
                    onItemClick?.invoke(absoluteAdapterPosition)
                    selectedPosition = absoluteAdapterPosition
                    notifyDataSetChanged()
                }
                root.setOnClickListener {
                    onItemClick?.invoke(absoluteAdapterPosition)
                    selectedPosition = absoluteAdapterPosition
                    notifyDataSetChanged()
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
    ): PlatformVoucherAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemPlatformVoucherBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlatformVoucherAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }

}