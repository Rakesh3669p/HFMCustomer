package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemBrandsBinding
import com.hfm.customer.ui.dashBoard.home.model.Brand
import com.hfm.customer.ui.dashBoard.home.model.Image
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class BrandStoreAdapter @Inject constructor() :
    RecyclerView.Adapter<BrandStoreAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBrandsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Brand) {
            with(bind) {
                val imageOriginal = data.brand_image
                adsImage.loadImage(replaceBaseUrl(imageOriginal))
                root.setOnClickListener {
                    onBrandClick?.let {
                        it(data.brand_id)
                    }
                }
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
    ): BrandStoreAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBrandsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BrandStoreAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onBrandClick: ((id: Int) -> Unit)? = null

    fun setOnBrandClickListener(listener: (id: Int) -> Unit) {
        onBrandClick = listener
    }

}