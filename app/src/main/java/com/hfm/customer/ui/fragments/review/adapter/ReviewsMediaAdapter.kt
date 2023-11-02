package com.hfm.customer.ui.fragments.review.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemAdsBinding
import com.hfm.customer.databinding.ItemAlphabetsBinding
import com.hfm.customer.databinding.ItemMediaBinding
import com.hfm.customer.ui.dashBoard.home.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.Brand
import javax.inject.Inject


class ReviewsMediaAdapter @Inject constructor() :
    RecyclerView.Adapter<ReviewsMediaAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var selectedIndex = 0

    inner class ViewHolder(private val bind: ItemMediaBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Uri) {
            with(bind) {
                media.setImageURI(data)
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewsMediaAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReviewsMediaAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: String) -> Unit)? = null

    fun setOnItemClickListener(listener: (alphabet: String) -> Unit) {
        onItemClick = listener
    }

}