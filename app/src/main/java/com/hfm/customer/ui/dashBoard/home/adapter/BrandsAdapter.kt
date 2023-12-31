package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemAdsBinding
import com.hfm.customer.ui.dashBoard.home.model.AdsImage
import com.hfm.customer.utils.loadImage

import javax.inject.Inject

class BrandsAdapter @Inject constructor(private val context: Context) :
    RecyclerView.Adapter<BrandsAdapter.ViewHolder>() {
    inner class ViewHolder(private val bind: ItemAdsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: AdsImage) {
            with(bind) {
                title.text = data.name
                adsImage.load(data.image)
                root.setOnClickListener { onItemClick?.invoke(absoluteAdapterPosition) }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<AdsImage>() {
        override fun areItemsTheSame(oldItem: AdsImage, newItem: AdsImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AdsImage, newItem: AdsImage): Boolean {
            return oldItem == newItem
        }
    }

    val differ:AsyncListDiffer<AdsImage> = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrandsAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(ItemAdsBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BrandsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }
}