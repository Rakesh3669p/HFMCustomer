package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemProductVariantsBinding
import javax.inject.Inject


class ProductVariantsAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductVariantsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductVariantsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:String) {
            with(bind) {
            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<String>(){
          override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
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
        holder.bind("")
//        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 3

    private var onBrandFilterClick: ((id: Int) -> Unit)? = null

    fun setOnBrandFilterClickListener(listener: (id: Int) -> Unit) {
        onBrandFilterClick = listener
    }

}