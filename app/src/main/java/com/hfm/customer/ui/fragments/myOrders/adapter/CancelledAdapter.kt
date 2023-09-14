package com.hfm.customer.ui.fragments.myOrders.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.hfm.customer.databinding.ItemBulkOrderBinding
import com.hfm.customer.ui.dashBoard.home.AdsImage
import com.hfm.customer.ui.dashBoard.home.model.Brand
import com.hfm.customer.ui.fragments.myOrders.model.BulkrequestOrderDetail
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeInvisible
import javax.inject.Inject


class CancelledAdapter @Inject constructor() :
    RecyclerView.Adapter<CancelledAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemBulkOrderBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: String) {
            with(bind) {
                requestStatusLbl.makeGone()
                requestStatus.text = "Cancelled on 20-02-2023"
                val orangeColor = ContextCompat.getColor(context, R.color.red)
                requestStatus.setTextColor(orangeColor)
                requestedDate.text = "RM 20,000.00"

                root.setOnClickListener {
                    onOrderClick?.let {
                        it(adapterPosition)
                    }
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
    ): CancelledAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemBulkOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CancelledAdapter.ViewHolder, position: Int) {
//        holder.bind(differ.currentList[position])
        holder.bind("")
    }

    override fun getItemCount(): Int = 4

    private var onOrderClick: ((id: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (id: Int) -> Unit) {
        onOrderClick = listener
    }
}