package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemProductVariantsBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import javax.inject.Inject


class ProductVariantsAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductVariantsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductVariantsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Variants) {
            with(bind) {
                val imageOriginal = data.image
                val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                productImage.load(imageReplaced)
                productVariant.text = data.combination
                root.setOnClickListener {
                    onVariantsClick?.let {
                        it(data.pro_id)
                    }
                }

            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Variants>(){
          override fun areItemsTheSame(oldItem: Variants, newItem: Variants): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Variants, newItem: Variants): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductVariantsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductVariantsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductVariantsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onVariantsClick: ((id: Int) -> Unit)? = null

    fun setOnVariantsClickListener(listener: (id: Int) -> Unit) {
        onVariantsClick = listener
    }

}