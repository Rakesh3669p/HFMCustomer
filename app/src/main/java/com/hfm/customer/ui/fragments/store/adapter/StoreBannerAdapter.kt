package com.hfm.customer.ui.fragments.store.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemBannerBinding
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject

class StoreBannerAdapter @Inject constructor(): RecyclerView.Adapter<StoreBannerAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val itemBannerBinding: ItemBannerBinding):RecyclerView.ViewHolder(itemBannerBinding.root){
        fun bind(data: String) {

            with(itemBannerBinding){
                bannerImage.loadImage(replaceBaseUrl(data) )
            }
        }
    }

   private val diffUtil = object :DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreBannerAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(ItemBannerBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: StoreBannerAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}