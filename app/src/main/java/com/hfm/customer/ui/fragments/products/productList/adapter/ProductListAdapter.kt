package com.hfm.customer.ui.fragments.products.productList.adapter

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemProductsBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.replaceBaseUrl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class ProductListAdapter @Inject constructor() :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemProductsBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {
                if (data.image?.isNotEmpty() == true) {
                    productImage.load(replaceBaseUrl(data.image[0].image)) {
                        placeholder(R.drawable.logo)

                    }
                }

                productName.text = data.product_name
                if (data.offer_price != null) {
                    if (data.offer_price.toString().isNotEmpty()) {
                        if (data.offer_price.toString() != "false") {
                            if (data.offer_price.toString().toDouble() > 0) {
                                productPrice.text = "RM ${
                                    formatToTwoDecimalPlaces(
                                        data.offer_price.toString().toDouble()
                                    )
                                }"
                            } else {
                                productPrice.text = "RM ${
                                    formatToTwoDecimalPlaces(
                                        data.actual_price.toString().toDouble()
                                    )
                                }"
                            }
                        } else {
                            productPrice.text = "RM ${
                                formatToTwoDecimalPlaces(
                                    (data.actual_price ?: "0").toString().toDouble()
                                )
                            }"
                        }
                    }


                } else {
                    if (data.actual_price != null) {
                        productPrice.text = "RM ${
                            formatToTwoDecimalPlaces(
                                data.actual_price.toString().toDouble()
                            )
                        }"
                    } else {
                        productPrice.isVisible = false
                    }
                }


                soldOut.isVisible =
                    data.is_out_of_stock.toString() == "true" || data.is_out_of_stock.toString() == "1"


                saleTime.isVisible = data.offer_name == "Flash Sale"
                if (saleTime.isVisible) {
                    setTimer(data.end_time, bind)
                }
                if (data.offer != null) {
                    saveLbl.isVisible = data.offer.toString().isNotEmpty() && data.offer != "false"
                }

                saveLbl.text = data.offer.toString()
                if (data.frozen != null)
                    frozenLbl.isVisible = data.frozen.toString().toDouble() > 0
                if (data.wholesale != null) wholeSaleLbl.isVisible =
                    data.wholesale.toString().toDouble() > 0


                root.setOnClickListener {
                    onProductClick?.let {
                        it(data.product_id.toString())
                    }
                }
            }
        }
    }

    private fun setTimer(endTime: String, bind: ItemProductsBinding) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = endTime
        val endTime = dateFormat.parse(endTimeString) ?: Date()
        val currentTime = Date()
        val timeDifference = endTime.time - currentTime.time
        val countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished, bind)
            }

            override fun onFinish() {
                updateCountdownText(0, bind)
            }
        }
        countdownTimer.start()
    }

    private fun updateCountdownText(millisUntilFinished: Long, bind: ItemProductsBinding) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000
        with(bind) {
            saleTime.text = "Ends In: ${
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d:%02d",
                    days,
                    hours,
                    minutes,
                    seconds
                )
            }"
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