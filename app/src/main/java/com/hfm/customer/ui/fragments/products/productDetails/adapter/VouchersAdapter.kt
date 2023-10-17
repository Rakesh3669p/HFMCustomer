package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemVoucherBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.SellerVoucherModelItem
import javax.inject.Inject


class VouchersAdapter @Inject constructor() :
    RecyclerView.Adapter<VouchersAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemVoucherBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:SellerVoucherModelItem) {
            with(bind) {
                discountPercent.text = data.offer
                discountDescription.text = "Min. Spend RM${data.minimum_purchase} Capped at ${data.offer_value}"
                voucherExpiry.text = "Expires on: ${data.valid_upto}"
                userNow.setOnClickListener {
                    onItemClick?.let {
                        it(adapterPosition)
                    }
                }
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<SellerVoucherModelItem>(){
          override fun areItemsTheSame(oldItem: SellerVoucherModelItem, newItem: SellerVoucherModelItem): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: SellerVoucherModelItem, newItem: SellerVoucherModelItem): Boolean {
              return oldItem == newItem
          }
      }
      val differ = AsyncListDiffer(this,diffUtil)


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