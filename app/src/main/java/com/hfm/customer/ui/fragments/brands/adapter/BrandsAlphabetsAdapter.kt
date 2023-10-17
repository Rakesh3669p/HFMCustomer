package com.hfm.customer.ui.fragments.brands.adapter

import android.content.Context
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
import com.hfm.customer.ui.dashBoard.home.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.Brand
import javax.inject.Inject


class BrandsAlphabetsAdapter @Inject constructor() :
    RecyclerView.Adapter<BrandsAlphabetsAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var selectedIndex = 0

    inner class ViewHolder(private val bind: ItemAlphabetsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
                title.text = data

                if(selectedIndex == absoluteAdapterPosition){
                    bg.isVisible = true
                    title.setTextColor(ContextCompat.getColor(context, R.color.white))
                }else{
                    bg.isVisible = false
                    title.setTextColor(ContextCompat.getColor(context, R.color.textGreyDark))
                }

                root.setOnClickListener {
                    onItemClick?.let {
                        it(data)
                    }
                    val previousIndex = selectedIndex
                    bg.isVisible = true
                    title.setTextColor(ContextCompat.getColor(context, R.color.white))
                    selectedIndex = absoluteAdapterPosition
                    notifyItemChanged(previousIndex)
                    notifyItemChanged(selectedIndex)
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
    ): BrandsAlphabetsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemAlphabetsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BrandsAlphabetsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((id: String) -> Unit)? = null

    fun setOnItemClickListener(listener: (alphabet: String) -> Unit) {
        onItemClick = listener
    }

}