package com.hfm.customer.ui.fragments.myOrders.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.databinding.ItemMyProductBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class MyAllOrdersProductsAdapter @Inject constructor() : RecyclerView.Adapter<MyAllOrdersProductsAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var from = ""
    private var isDeliveredOrder = false
    inner class ViewHolder(private val bind: ItemMyProductBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun bind(data: Product) {
            with(bind) {
                if(data.product_image?.isNotEmpty() == true){
                    productImage.loadImage(replaceBaseUrl(data.product_image[0].image))
                }
                productName.text = data.product_name
                if(data.product_type.lowercase()=="config"){
                    productQty.text = "${data.attr_name1} | Qty: ${data.quantity.toString().toDouble().roundToInt()}"
                }else{
                    productQty.text = "Qty: ${data.quantity.toString().toDouble().roundToInt()}"
                }

                amount.isVisible = from == "orderDetails"
                if(data.sale_price!=null){
                    amount.text = "RM ${formatToTwoDecimalPlaces(data.sale_price.toString().toDouble() * data.quantity.toString().toDouble().roundToInt())}"
                }else{
                    amount.text = "RM ${formatToTwoDecimalPlaces(data.actual_price.toString().toDouble()* data.quantity.toString().toDouble().roundToInt())}"
                }

                rateProduct.isVisible = data.review_submitted == 0 && isDeliveredOrder
                rateProduct.setOnClickListener {
                    onRateProductClick?.invoke(data.product_id.toString())
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAllOrdersProductsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemMyProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyAllOrdersProductsAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun setDataFrom(status:String){
        from = status
    }

    private var onRateProductClick: ((id: String) -> Unit)? = null

    fun setOnRateProductClickListener(listener: (id: String) -> Unit) {
        onRateProductClick = listener
    }

    fun setDeliveredOrder(orderDelivered:Boolean){
        isDeliveredOrder = orderDelivered
    }
}