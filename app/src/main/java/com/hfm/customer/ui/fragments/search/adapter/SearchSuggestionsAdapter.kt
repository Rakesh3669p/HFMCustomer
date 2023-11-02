package com.hfm.customer.ui.fragments.search.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemSearchTextBinding
import com.hfm.customer.ui.fragments.search.model.RelatedTerm
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


class SearchSuggestionsAdapter @Inject constructor() :
    RecyclerView.Adapter<SearchSuggestionsAdapter.ViewHolder>() {
    private lateinit var context: Context
    var searchValue =""
    inner class ViewHolder(private val bind: ItemSearchTextBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: RelatedTerm) {
            with(bind) {
                val spannableString = SpannableString(data.name)
                val pattern: Pattern = Pattern.compile(Pattern.quote(searchValue), Pattern.CASE_INSENSITIVE)
                val matcher: Matcher = pattern.matcher(data.name)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
                    spannableString.setSpan(ForegroundColorSpan(Color.BLACK), start, end, 0)
                }
                seachText.text = spannableString


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

    fun setOnSearchValue(search:String) {
        searchValue = search
    }

}