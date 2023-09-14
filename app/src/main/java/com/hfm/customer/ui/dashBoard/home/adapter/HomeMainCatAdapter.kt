package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.commonModel.CatSubcat
import com.hfm.customer.databinding.ItemHomeCategoryBinding
import com.hfm.customer.databinding.ItemHomeMainCatBinding
import javax.inject.Inject


class HomeMainCatAdapter @Inject constructor() :
    RecyclerView.Adapter<HomeMainCatAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemHomeMainCatBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: CatSubcat) {
            with(bind) {
                catImage.load(data.image)
                catName.text = data.category_name

                root.setOnClickListener {
                    onCategoryClick?.let {
                        it(data)
                    }
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
    ): HomeMainCatAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemHomeMainCatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeMainCatAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCategoryClick: ((id: CatSubcat) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (id: CatSubcat) -> Unit) {
        onCategoryClick = listener
    }

}