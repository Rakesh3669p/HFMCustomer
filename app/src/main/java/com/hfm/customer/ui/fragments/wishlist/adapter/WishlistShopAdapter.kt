package com.hfm.customer.ui.fragments.wishlist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemBannerBinding
import com.hfm.customer.databinding.ItemShopsBinding
import javax.inject.Inject

class WishlistShopAdapter @Inject constructor(): RecyclerView.Adapter<WishlistShopAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class  ViewHolder(private val itemBannerBinding: ItemShopsBinding):RecyclerView.ViewHolder(itemBannerBinding.root){
        fun bind(data: String) {

            with(itemBannerBinding){

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ItemShopsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(differ.currentList[position])
        holder.bind("")
    }

    override fun getItemCount(): Int = 5
}