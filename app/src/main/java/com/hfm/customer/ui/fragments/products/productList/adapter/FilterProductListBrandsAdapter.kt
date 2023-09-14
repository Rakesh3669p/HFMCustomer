package com.hfm.customer.ui.fragments.products.productList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemFilterBrandBinding
import com.hfm.customer.ui.fragments.products.productList.ProductListFragment.Companion.selectedBrandFilters
import javax.inject.Inject


class FilterProductListBrandsAdapter @Inject constructor() :
    RecyclerView.Adapter<FilterProductListBrandsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemFilterBrandBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:String) {
            with(bind) {
                bradName.text = data


                bradName.setOnClickListener {
                    onBrandFilterClick?.let {
                        it(adapterPosition)
                    }
                }

                if(selectedBrandFilters.contains(adapterPosition) ) {
                    bradName.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                    bradName.setTextColor(ContextCompat.getColor(context, R.color.white))
                }else{
                    bradName.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey_lite)
                    bradName.setTextColor(ContextCompat.getColor(context, R.color.black))
                }

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
    ): FilterProductListBrandsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemFilterBrandBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilterProductListBrandsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onBrandFilterClick: ((id: Int) -> Unit)? = null

    fun setOnBrandFilterClickListener(listener: (id: Int) -> Unit) {
        onBrandFilterClick = listener
    }

}