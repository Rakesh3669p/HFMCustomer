package com.hfm.customer.ui.fragments.products.productDetails.adapter

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
import com.hfm.customer.databinding.ItemProductsSingleBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.OtherProduct
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import javax.inject.Inject


class RelativeProductListAdapter @Inject constructor() :
    RecyclerView.Adapter<RelativeProductListAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductsSingleBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: OtherProduct) {
            with(bind) {
                if(data.image.isNotEmpty()) {
                    val imageOriginal = data.image[0].image
                    val imageReplaced = imageOriginal.replace("https://uat.hfm.synuos.com", "http://4.194.191.242")
                    productImage.loadImage(imageReplaced)
                }


                productName.text = data.product_name
                productPrice.text = "RM ${formatToTwoDecimalPlaces(data.actual_price.toString().toDouble()) }"

                saveLbl.isVisible = false
                frozenLbl.isVisible = false
                wholeSaleLbl.isVisible = false
//                saveLbl.isVisible = data.offer.isNotEmpty() && data.offer!="false"
//                saveLbl.text = data.offer
//                frozenLbl.isVisible = data.frozen==1
//                wholeSaleLbl.isVisible = data.wholesale==1


                root.setOnClickListener {
                    onProductClick?.let {
                        it(data.product_id.toString())
                    }
                }

            }
        }
    }

      private val diffUtil = object : DiffUtil.ItemCallback<OtherProduct>(){
          override fun areItemsTheSame(oldItem: OtherProduct, newItem: OtherProduct): Boolean {
              return oldItem == newItem
          }

          override fun areContentsTheSame(oldItem: OtherProduct, newItem: OtherProduct): Boolean {
              return oldItem == newItem
          }

      }
      val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RelativeProductListAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemProductsSingleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RelativeProductListAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
    override fun getItemViewType(position: Int): Int = position

    private var onProductClick: ((id: String) -> Unit)? = null


    fun setOnProductClickListener(listener: (id: String) -> Unit) {
        onProductClick = listener
    }
}