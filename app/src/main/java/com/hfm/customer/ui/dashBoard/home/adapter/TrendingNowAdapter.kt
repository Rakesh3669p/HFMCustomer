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
import com.hfm.customer.databinding.ItemTrendingNowBinding
import com.hfm.customer.ui.dashBoard.home.model.Events
import com.hfm.customer.utils.loadImage
import javax.inject.Inject


class TrendingNowAdapter @Inject constructor() :
    RecyclerView.Adapter<TrendingNowAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemTrendingNowBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Events) {
            with(bind) {
                val imageOriginal = data.image
                val imageReplaced =
                    imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                productImage.loadImage(imageReplaced)

                root.setOnClickListener {
                    onCategoryClick?.invoke(data.category, data.sub_category)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Events>() {
        override fun areItemsTheSame(oldItem: Events, newItem: Events): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Events, newItem: Events): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrendingNowAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemTrendingNowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TrendingNowAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCategoryClick: ((cat: String, subCat: String) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (cat: String, subCat: String) -> Unit) {
        onCategoryClick = listener
    }

}