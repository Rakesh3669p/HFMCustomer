package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemAdsBinding
import com.hfm.customer.ui.dashBoard.home.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.Brand
import javax.inject.Inject


class BrandsAdapter @Inject constructor() :
    RecyclerView.Adapter<BrandsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemAdsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: AdsImage) {
            with(bind) {
                adsImage.load(data.image)
                title.text = data.name
                root.setOnClickListener {
                    onItemClick?.let {
                        it(adapterPosition)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<AdsImage>() {
        override fun areItemsTheSame(oldItem: AdsImage, newItem: AdsImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AdsImage   , newItem: AdsImage): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BrandsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemAdsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BrandsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }

}