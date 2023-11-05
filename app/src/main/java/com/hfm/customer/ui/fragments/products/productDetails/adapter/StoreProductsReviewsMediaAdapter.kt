package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemReviewMediaBinding
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject


class StoreProductsReviewsMediaAdapter @Inject constructor() :
    RecyclerView.Adapter<StoreProductsReviewsMediaAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemReviewMediaBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
                reviewThumbnail.load(replaceBaseUrl(data)){
                    placeholder(R.drawable.logo)
                }
                root.setOnClickListener {
                    onItemClick?.invoke(data)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreProductsReviewsMediaAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemReviewMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StoreProductsReviewsMediaAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((imageLink: String) -> Unit)? = null

    fun setOnItemClickListener(listener: (imageLink: String) -> Unit) {
        onItemClick = listener
    }

}