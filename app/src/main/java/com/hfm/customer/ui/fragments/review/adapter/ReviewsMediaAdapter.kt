package com.hfm.customer.ui.fragments.review.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemMediaBinding
import com.hfm.customer.utils.loadImage
import java.io.File
import javax.inject.Inject


class ReviewsMediaAdapter @Inject constructor() :
    RecyclerView.Adapter<ReviewsMediaAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemMediaBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: File) {
            with(bind) {
                media.load(data)
                removeMedia.setOnClickListener {
                 onItemClick?.invoke(absoluteAdapterPosition)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
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

    private var onItemClick: ((position: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (position: Int) -> Unit) {
        onItemClick = listener
    }

}