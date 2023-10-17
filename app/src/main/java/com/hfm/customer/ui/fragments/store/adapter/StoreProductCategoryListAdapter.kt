package com.hfm.customer.ui.fragments.store.adapter

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
import com.hfm.customer.ui.fragments.store.model.Category
import javax.inject.Inject


class StoreProductCategoryListAdapter @Inject constructor() :
    RecyclerView.Adapter<StoreProductCategoryListAdapter.ViewHolder>() {
    private lateinit var context: Context
    var selectedPosition = 0

    inner class ViewHolder(private val bind: ItemListCategoriesBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data:Category) {
            with(bind) {

                title.text = data.name

                if (selectedPosition == absoluteAdapterPosition) {
                    title.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                    title.setTextColor(ContextCompat.getColorStateList(context, R.color.white))
                } else {
                    title.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.grey_lite)
                    title.setTextColor(ContextCompat.getColorStateList(context, R.color.black))
                }

                title.setOnClickListener {
                    selectedPosition = absoluteAdapterPosition
                    onSubCategoryClick?.let {
                        it(data.id)
                    }
                    notifyDataSetChanged()
                }


            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<Category>(){
          override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreProductCategoryListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemListCategoriesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoreProductCategoryListAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onSubCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnSubCategoryClickListener(listener: (id: Int) -> Unit) {
        onSubCategoryClick = listener
    }

}