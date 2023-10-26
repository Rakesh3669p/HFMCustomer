package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemProductVariantsBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class ProductVariantsAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductVariantsAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var selectedPosition = 0

    inner class ViewHolder(private val bind: ItemProductVariantsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Variants) {
            with(bind) {
                productImage.isVisible = data.image.isNotEmpty()
                productImage.load(replaceBaseUrl(data.image)){
                    placeholder(R.drawable.logo)
                    
                }
                productVariant.text = data.combination
                if(data.isSelected){
                    mainLayout.background = ContextCompat.getDrawable(context, R.drawable.outline_line_box_red)
                    productVariant.setTextColor(ContextCompat.getColor(context, R.color.red))
                }else{
                    mainLayout.background = ContextCompat.getDrawable(context, R.drawable.outline_line_box_white)
                    productVariant.setTextColor(ContextCompat.getColor(context, R.color.black))
                }

                root.setOnClickListener {
                    differ.currentList.forEach { it.isSelected = false }
                    data.isSelected =true
                    onVariantsClick?.invoke(absoluteAdapterPosition)
                    notifyDataSetChanged()
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