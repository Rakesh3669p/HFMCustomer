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
import com.hfm.customer.commonModel.MainBanner
import com.hfm.customer.databinding.ItemBannerBinding
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class HomeMainBannerAdapter @Inject constructor() :
    RecyclerView.Adapter<HomeMainBannerAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBannerBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: MainBanner) {
            with(bind) {
                bannerImage.roundPercent = 0.2F
                bannerImage.loadImage(replaceBaseUrl(data.media))
                root.setOnClickListener {
                    onItemClick?.invoke(data.category,data.sub_category,data.product_id,data.link_type)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<MainBanner>() {
        override fun areItemsTheSame(oldItem: MainBanner, newItem: MainBanner): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MainBanner, newItem: MainBanner): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeMainBannerAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBannerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeMainBannerAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onItemClick: ((category: String,subCategory: String,productId:String,linkType:String) -> Unit)? = null

    fun setOnItemClickListener(listener: (category: String,subCategory: String,productId:String,linkType:String) -> Unit) {
        onItemClick = listener
    }

}