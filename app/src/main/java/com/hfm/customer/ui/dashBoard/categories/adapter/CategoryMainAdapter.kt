package com.hfm.customer.ui.dashBoard.categories.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.commonModel.CatSubcat
import com.hfm.customer.databinding.ItemCategoryMainBinding
import javax.inject.Inject


class CategoryMainAdapter @Inject constructor() :
    RecyclerView.Adapter<CategoryMainAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var selectedPosition = 0

    inner class ViewHolder(private val bind: ItemCategoryMainBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: CatSubcat) {
            with(bind) {
                categoryName.text = data.category_name
                categoryImage.load(data.image){
                    placeholder(R.drawable.logo)
                    
                }

                itemCv.cardElevation = 0F
                categoryName.setTextColor(ContextCompat.getColor(context, R.color.black))

                if (selectedPosition == absoluteAdapterPosition) {
                    itemCv.cardElevation = 4F
                    categoryName.setTextColor(ContextCompat.getColor(context, R.color.red))
                }

                itemCv.setOnClickListener {
                    onMainCategoryClick?.let {
                        it(absoluteAdapterPosition)
                    }
                    selectedPosition = absoluteAdapterPosition
                    notifyDataSetChanged()

                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<CatSubcat>() {
        override fun areItemsTheSame(oldItem: CatSubcat, newItem: CatSubcat): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CatSubcat, newItem: CatSubcat): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryMainAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCategoryMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryMainAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onMainCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnMainCategoryClickListener(listener: (id: Int) -> Unit) {
        onMainCategoryClick = listener
    }
}