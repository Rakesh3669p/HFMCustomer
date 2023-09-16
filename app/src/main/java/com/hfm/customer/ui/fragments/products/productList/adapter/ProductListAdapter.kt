package com.hfm.customer.ui.fragments.products.productList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hfm.customer.databinding.ItemProductsBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import javax.inject.Inject
import kotlin.math.roundToInt


class ProductListAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {
                if (data.image.isNotEmpty()) {
                    val imageOriginal = data.image[0].image
                    val imageReplaced =
                        imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.load(imageReplaced)
                }
                productName.text = data.product_name
                if(data.offer_price!=null) {
                    if (data.offer_price.toString().isNotEmpty()) {
                        if (data.offer_price.toString() != "false") {
                            if (data.offer_price.toString().toDouble() > 0) {
                                productPrice.text = "RM ${data.offer_price.toString().toDouble()}"
                            } else {
                                productPrice.text = "RM ${data.actual_price.toString().toDouble()}"
                            }
                        } else {
                            productPrice.text = "RM ${data.actual_price.toString().toDouble()}"
                        }
                    }
                }else{
                    productPrice.text = "RM ${data.actual_price.toString().toDouble()}"
                }


                if(data.offer!=null) {
                    saveLbl.isVisible = data.offer.toString().isNotEmpty() && data.offer != "false"
                }

                saveLbl.text = data.offer.toString()
                frozenLbl.isVisible = data.frozen == 1
                wholeSaleLbl.isVisible = data.wholesale == 1


                root.setOnClickListener {
                    onProductClick?.let {
                        it(data.product_id.toString())
                    }
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
    ): ProductListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductListAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onProductClick: ((id: String) -> Unit)? = null


    fun setOnProductClickListener(listener: (id: String) -> Unit) {
        onProductClick = listener
    }
}