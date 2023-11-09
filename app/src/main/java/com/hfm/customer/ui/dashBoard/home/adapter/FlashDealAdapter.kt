package com.hfm.customer.ui.dashBoard.home.adapter

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemProductsSingleBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeGone
import com.hfm.customer.utils.makeVisible
import javax.inject.Inject


class FlashDealAdapter @Inject constructor() :
    RecyclerView.Adapter<FlashDealAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductsSingleBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {

                if(data.image?.isNotEmpty() == true) {
                    val imageOriginal = data.image[0].image
                    val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.loadImage(imageReplaced)
                }
                productName.text = data.product_name

                if(data.offer_price!=null&&data.offer_price.toString() !="false"&&data.offer_price.toString().toDouble()>0){
                    productPrice.text = "RM ${formatToTwoDecimalPlaces(data.offer_price.toString().toDouble())}"
                }else{
                    productPrice.text = "RM ${formatToTwoDecimalPlaces(data.actual_price.toString().toDouble())}"
                }

                soldOut.isVisible = data.is_out_of_stock==1


                if(data.frozen==1){
                    frozenLbl.makeVisible()
                    frozenLbl.text = "Fresh/Frozen"
                }else if(data.chilled==1){
                    frozenLbl.makeVisible()
                    val filters = arrayOfNulls<InputFilter>(10)
                    filters[0] = InputFilter.LengthFilter(android.R.attr.maxLength)
                    frozenLbl.filters = filters
                    frozenLbl.text = "Chilled"
                }else{
                    frozenLbl.makeGone()
                }
                wholeSaleLbl.isVisible =data.wholesale.toString().toDouble() > 0

                if(data.offer_price!=null&&data.offer_price.toString() !="false"&&data.offer_price.toString().toDouble()>0){
                    val difference = data.actual_price.toString().toDouble() - data.offer_price.toString().toDouble()
                    saveLbl.isVisible = difference>0
                    saveLbl.text = "Save RM ${formatToTwoDecimalPlaces(difference)}"
                }else{
                    saveLbl.isVisible = false
                }

                root.setOnClickListener {
                    onProductClick?.let {
                        it(data.product_id.toString().toInt())
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
    ): FlashDealAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductsSingleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FlashDealAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onProductClick: ((id: Int) -> Unit)? = null

    fun setOnProductClickListener(listener: (id: Int) -> Unit) {
        onProductClick = listener
    }

}


