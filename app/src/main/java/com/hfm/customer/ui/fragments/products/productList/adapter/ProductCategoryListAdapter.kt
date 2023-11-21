package com.hfm.customer.ui.fragments.products.productList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemListCategoriesBinding
import com.hfm.customer.ui.fragments.products.productList.model.Subcategory
import javax.inject.Inject


class ProductCategoryListAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductCategoryListAdapter.ViewHolder>() {
    private lateinit var context: Context
    var selectedPosition = 0

    inner class ViewHolder(private val bind: ItemListCategoriesBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Subcategory) {
            with(bind) {

                title.text = data.subcategory_name

                if (selectedPosition == adapterPosition) {
                    title.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                    title.setTextColor(ContextCompat.getColorStateList(context, R.color.white))
                } else {
                    title.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.grey_lite)
                    title.setTextColor(ContextCompat.getColorStateList(context, R.color.black))
                }

                title.setOnClickListener {
                    selectedPosition = adapterPosition
                    onSubCategoryClick?.let {
                        it(data.id)
                    }
                    notifyDataSetChanged()
                }


            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Subcategory>(){
          override fun areItemsTheSame(oldItem: Subcategory, newItem: Subcategory): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Subcategory, newItem: Subcategory): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductCategoryListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemListCategoriesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductCategoryListAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onSubCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnSubCategoryClickListener(listener: (id: Int) -> Unit) {
        onSubCategoryClick = listener
    }

}