package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemProductsSingleBinding
import com.hfm.customer.ui.dashBoard.home.model.Product
import javax.inject.Inject


class FlashDealAdapter @Inject constructor() :
    RecyclerView.Adapter<FlashDealAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductsSingleBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {

                if(data.image.isNotEmpty()) {
                    val imageOriginal = data.image[0].image
                    val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.load(imageReplaced)
                }
                productName.text = data.product_name
                productPrice.text = "RM ${data.actual_price}"
                if(adapterPosition==0){
                    wholeSaleLbl.isVisible = true
                    saveLbl.isVisible = true
                    frozenLbl.isVisible = true
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FlashDealAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductsSingleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FlashDealAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (id: Int) -> Unit) {
        onCategoryClick = listener
    }

}