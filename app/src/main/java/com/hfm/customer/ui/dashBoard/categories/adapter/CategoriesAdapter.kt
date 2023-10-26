package com.hfm.customer.ui.dashBoard.categories.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.commonModel.Subcategory
import com.hfm.customer.databinding.ItemCategoryItemsBinding
import com.hfm.customer.databinding.ItemCategoryMainBinding
import javax.inject.Inject


class CategoriesAdapter @Inject constructor() :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemCategoryItemsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Subcategory) {
            with(bind) {
                categoryName.text = data.subcategory_name
                categoryImage.load(data.subcategory_image){
                    placeholder(R.drawable.logo)
                    
                }
                root.setOnClickListener {
                   onCategoryClick?.let { it(data.id) }
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
    ): CategoriesAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCategoryItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoriesAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (id: Int) -> Unit) {
        onCategoryClick = listener
    }

}