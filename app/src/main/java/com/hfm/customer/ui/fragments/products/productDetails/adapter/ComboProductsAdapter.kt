package com.hfm.customer.ui.fragments.products.productDetails.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemComboBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.ComboProducts
import javax.inject.Inject


class ComboProductsAdapter @Inject constructor() :
    RecyclerView.Adapter<ComboProductsAdapter.ViewHolder>() {
    private lateinit var context: Context
    inner class ViewHolder(private val bind: ItemComboBinding) :
        RecyclerView.ViewHolder(bind.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: ComboProducts) {
            with(bind) {
                comboName.text = data.product_name
                comboQty.text = "Quantity: ${data.individual_qty}"

                root.setOnClickListener {
                    onItemClick?.invoke(data.product_id)
                    notifyDataSetChanged()
                }

            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<ComboProducts>(){
          override fun areItemsTheSame(oldItem: ComboProducts, newItem: ComboProducts): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: ComboProducts, newItem: ComboProducts): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ComboProductsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemComboBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ComboProductsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onItemClick: ((id: Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (id: Int) -> Unit) {
        onItemClick = listener
    }
}