package com.hfm.customer.ui.fragments.payment.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemFaqBinding
import com.hfm.customer.databinding.ItemMyOrdersBinding
import com.hfm.customer.ui.fragments.myOrders.model.Purchase
import com.hfm.customer.ui.fragments.payment.model.Faq
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject


class PaymentsFAQAdapter @Inject constructor() : RecyclerView.Adapter<PaymentsFAQAdapter.ViewHolder>() {

    private lateinit var context: Context
    var selectedPosition: Int = -1

    inner class ViewHolder(private val bind: ItemFaqBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Faq) {

            with(bind) {
                title.text = data.question
                description.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(data.answer, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(data.answer)
                }

                if(selectedPosition==absoluteAdapterPosition){
                    titleArrow.rotation = 90F
                    descriptionLayout.isVisible = true
                }else{
                    titleArrow.rotation = 0F
                    descriptionLayout.isVisible = false
                }

                root.setOnClickListener {
                    selectedPosition = if(selectedPosition == absoluteAdapterPosition){
                        -1
                    }else{
                        absoluteAdapterPosition
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Faq>() {
        override fun areItemsTheSame(
            oldItem: Faq,
            newItem: Faq
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Faq,
            newItem: Faq
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentsFAQAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemFaqBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PaymentsFAQAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onOrderClick: ((id: Int) -> Unit)? = null

    fun setOnOrderClickListener(listener: (id: Int) -> Unit) {
        onOrderClick = listener
    }

}