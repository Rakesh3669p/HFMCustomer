package com.hfm.customer.ui.fragments.checkOut.adapter

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemCartProductBinding
import com.hfm.customer.databinding.ItemCheckoutProductBinding
import com.hfm.customer.databinding.ItemProductsBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.replaceBaseUrl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


class CheckOutProductAdapter @Inject constructor() :
    RecyclerView.Adapter<CheckOutProductAdapter.ViewHolder>() {
    private var selectAll: Boolean = false
    private lateinit var context: Context

    inner class ViewHolder(private val bind: ItemCheckoutProductBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {
                if (!data.image.isNullOrEmpty()) {
                    productImage.loadImage(replaceBaseUrl(data.image[0].image))
                } else if (!data.product_image.isNullOrEmpty()) {
                    productImage.loadImage(replaceBaseUrl(data.product_image[0].image))
                }
                productName.text = data.product_name
                available.isVisible = data.check_shipping_availability.toString().toDouble() < 1 && data.cart_selected.toString().toDouble() > 0
                available.text = data.check_shipping_availability_text



                if(data.check_shipping_availability.toString().toDouble()>0){
                    available.setTextColor(ContextCompat.getColor(context,R.color.black))
                }else{
                    available.setTextColor(ContextCompat.getColor(context,R.color.red))
                }

                if (data.total_offer_price.toString().toDouble() > 0) {
                    productPrice.text = "RM ${
                        formatToTwoDecimalPlaces(
                            data.total_offer_price.toString().toDouble()
                        )
                    }"
                } else {
                    productPrice.text =
                        "RM ${formatToTwoDecimalPlaces(data.total_actual_price.toString().toDouble())}"
                }


                if (data.attr_name1.isNullOrEmpty()) {
                    variants.text = "Qty: ${data.quantity.toString().toDouble().roundToInt()}"
                } else {
                    variants.text = "${data.attr_name1} | Qty: ${data.quantity.toString().toDouble().roundToInt()}"
                }

                flashDeal.isVisible = (data.offer_name == "Flash Sale" || data.offer_name =="Shocking Sale")
                if (flashDeal.isVisible) {
                    setTimer(data.end_time, flashDeal)
                }
            }
        }
    }

    private fun setTimer(endTime: String, flashDeal: TextView) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endTimeString = endTime
        val endTime = dateFormat.parse(endTimeString) ?: Date()
        val currentTime = Date()
        val timeDifference = endTime.time - currentTime.time
        val countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished, flashDeal)
            }

            override fun onFinish() {
                updateCountdownText(0, flashDeal)
            }
        }
        countdownTimer.start()
    }


    private fun updateCountdownText(millisUntilFinished: Long, flashDeal: TextView) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

            flashDeal.text = "Flash Deals Ends In: ${
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
    ): CheckOutProductAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCheckoutProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CheckOutProductAdapter.ViewHolder, position: Int) {
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

    private var onVariantClick: ((variants: List<Variants>, cartId: String) -> Unit)? = null

    fun setOnVariantClick(listener: (variants: List<Variants>, cartId: String) -> Unit) {
        onVariantClick = listener
    }

    fun selectAllProducts(status: Boolean) {
        selectAll = status
        notifyDataSetChanged()
    }


    fun setShippingOptions() {

    }
}