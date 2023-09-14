package com.hfm.customer.ui.fragments.brands.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemBrandsBinding
import com.hfm.customer.databinding.ItemBrandsSingleBinding
import com.hfm.customer.ui.dashBoard.home.model.Brand
import com.hfm.customer.ui.dashBoard.home.model.Image
import javax.inject.Inject


class BrandStoreSingleAdapter @Inject constructor() :
    RecyclerView.Adapter<BrandStoreSingleAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBrandsSingleBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Brand) {
            with(bind) {
                val imageOriginal = data.brand_image
                val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                brandImage.load(imageReplaced)
                title.text = data.brand_name
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Brand>() {
        override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrandStoreSingleAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBrandsSingleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BrandStoreSingleAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCategoryClick: ((id: Int) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (id: Int) -> Unit) {
        onCategoryClick = listener
    }

}