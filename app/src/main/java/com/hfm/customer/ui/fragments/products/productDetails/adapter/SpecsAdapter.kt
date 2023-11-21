package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemReviewCommentsBinding
import com.hfm.customer.databinding.ItemSpecsBinding
import com.hfm.customer.databinding.ItemVoucherBinding
import javax.inject.Inject


class SpecsAdapter @Inject constructor() :
    RecyclerView.Adapter<SpecsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemSpecsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
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
    ): SpecsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemSpecsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SpecsAdapter.ViewHolder, position: Int) {
        holder.bind("")
//        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = 5
    override fun getItemViewType(position: Int): Int = position

    private var onBrandFilterClick: ((id: Int) -> Unit)? = null

    fun setOnBrandFilterClickListener(listener: (id: Int) -> Unit) {
        onBrandFilterClick = listener
    }

}