package com.hfm.customer.ui.fragments.cart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemCartProductBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject
import kotlin.math.roundToInt


class CartProductAdapter @Inject constructor() :
    RecyclerView.Adapter<CartProductAdapter.ViewHolder>() {
    private var selectAll: Boolean = false
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemCartProductBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {
                if (!data.image.isNullOrEmpty()) {
                    productImage.load(replaceBaseUrl(data.image[0].image))
                } else if (!data.product_image.isNullOrEmpty()) {
                    productImage.load(replaceBaseUrl(data.product_image[0].image))
                }
                productName.text = data.product_name
                productPrice.text = "RM ${formatToTwoDecimalPlaces(data.total_discount_price.toString().toDouble())}"
                qty.text = data.quantity.toString().toDouble().roundToInt().toString()
                variant.isVisible = data.variants_list.isNotEmpty()

                available.isVisible = data.check_shipping_availability.toString().toDouble() < 1 && data.cart_selected.toString().toDouble()>0
                available.text  = data.check_shipping_availability_text

                if (data.variants_list != null && data.variants_list.isNotEmpty()) {
                    variant.text = data.attr_name1
                }

                soldOut.isVisible = data.is_out_of_stock

                increaseQty.setOnClickListener {
                    onQtyChange?.let {
                        it(data.cart_id.toString(), (qty.text.toString().toInt() + 1).toString())
                    }
                }

                decreaseQty.setOnClickListener {
                    onQtyChange?.let {
                        it(data.cart_id.toString(), (qty.text.toString().toInt() - 1).toString())
                    }
                }

                checkBox.isChecked = data.cart_selected.toString().toDouble() > 0

                checkBox.setOnClickListener {
                    data.cart_selected = 1
                    onProductSelection?.let {
                        it(data.cart_id.toString(), checkBox.isChecked)
                    }
                }

                delete.setOnClickListener {
                    onDeleteClick?.let {
                        it(data.cart_id.toString())
                    }
                }

                variant.setOnClickListener {
                    data.variants_list.forEach {
                        if(data.attr_name1.contains(it.combination)){
                            it.isSelected = true
                        }
                    }
                    onVariantClick?.invoke(data.variants_list, data.cart_id.toString())
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartProductAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCartProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onDeleteClick: ((id: String) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (id: String) -> Unit) {
        onDeleteClick = listener
    }

    private var onQtyChange: ((id: String, qty: String) -> Unit)? = null

    fun setOnQtyChangeListener(listener: (id: String, qty: String) -> Unit) {
        onQtyChange = listener
    }

    private var onProductSelection: ((id: String, status: Boolean) -> Unit)? = null

    fun setOnProductSelection(listener: (id: String, status: Boolean) -> Unit) {
        onProductSelection = listener
    }

    private var onVariantClick: ((variants: List<Variants>,cartId:String) -> Unit)? = null

    fun setOnVariantClick(listener: (variants: List<Variants>,cartId:String) -> Unit) {
        onVariantClick = listener
    }

    fun selectAllProducts(status: Boolean) {
        selectAll = status
        notifyDataSetChanged()
    }

}