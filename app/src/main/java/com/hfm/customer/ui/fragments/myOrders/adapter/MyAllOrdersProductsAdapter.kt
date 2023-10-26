package com.hfm.customer.ui.fragments.myOrders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemMyProductBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.replaceBaseUrl
import javax.inject.Inject
import kotlin.math.roundToInt


class MyAllOrdersProductsAdapter @Inject constructor() : RecyclerView.Adapter<MyAllOrdersProductsAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var from = ""

    inner class ViewHolder(private val bind: ItemMyProductBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {
                if(data.product_image?.isNotEmpty() == true){
                    productImage.load(replaceBaseUrl(data.product_image[0].image)){
                        placeholder(R.drawable.logo)
                        
                    }
                }
                productName.text = data.product_name
                productQty.text = "Quantity: ${data.quantity.toString().toDouble().roundToInt()}"
                amount.isVisible = from == "orderDetails"
                if(data.sale_price!=null){

                    amount.text = "RM ${formatToTwoDecimalPlaces(data.sale_price.toString().toDouble())}"
                }else{
                    amount.text = "RM ${formatToTwoDecimalPlaces(data.actual_price.toString().toDouble())}"
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
}