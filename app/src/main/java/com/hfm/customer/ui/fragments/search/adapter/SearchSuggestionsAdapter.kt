package com.hfm.customer.ui.fragments.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemFilterBrandBinding
import com.hfm.customer.databinding.ItemSearchTextBinding
import com.hfm.customer.ui.fragments.products.productList.ProductListFragment.Companion.selectedBrandFilters
import com.hfm.customer.ui.fragments.search.model.RelatedTerm
import javax.inject.Inject


class SearchSuggestionsAdapter @Inject constructor() :
    RecyclerView.Adapter<SearchSuggestionsAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemSearchTextBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: RelatedTerm) {
            with(bind) {
                seachText.text = data.name
                root.setOnClickListener {
                    onItemClick?.let {
                        it(data.name)
                    }
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<RelatedTerm>() {
        override fun areItemsTheSame(oldItem: RelatedTerm, newItem: RelatedTerm): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RelatedTerm, newItem: RelatedTerm): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuggestionsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemSearchTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchSuggestionsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onItemClick: ((searchTerm: String) -> Unit)? = null

    fun setOnItemClickListener(listener: (searchTerm: String) -> Unit) {
        onItemClick = listener
    }

}