package com.hfm.customer.ui.fragments.cart.adapter

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemCartProductBinding
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.loadImage
import com.hfm.customer.utils.makeInvisible
import com.hfm.customer.utils.makeVisible
import com.hfm.customer.utils.replaceBaseUrl
import com.hfm.customer.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


class CartProductAdapter @Inject constructor() :
    RecyclerView.Adapter<CartProductAdapter.ViewHolder>() {
    private var selectAll: Boolean = false
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var countdownTimer: CountDownTimer? = null

    inner class ViewHolder(private val bind: ItemCartProductBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: Product) {
            with(bind) {

                if (!data.image.isNullOrEmpty()) {
                    productImage.loadImage(replaceBaseUrl(data.image[0].image))
                } else if (!data.product_image.isNullOrEmpty()) {
                    productImage.loadImage(replaceBaseUrl(data.product_image[0].image))
                }

                productName.text = data.product_name

                if (data.total_offer_price.toString().toDouble() > 0) {
                    productPrice.text = "RM ${
                        formatToTwoDecimalPlaces(
                            data.total_offer_price.toString().toDouble()
                        )
                    }"
                } else {
                    productPrice.text = "RM ${
                        formatToTwoDecimalPlaces(
                            data.total_actual_price.toString().toDouble()
                        )
                    }"
                }

                qty.text = data.quantity.toString().toDouble().roundToInt().toString()
                variant.isVisible = data.variants_list?.isNotEmpty() == true

                available.isVisible = data.check_shipping_availability.toString()
                    .toDouble() < 1 && data.cart_selected.toString().toDouble() > 0
                available.text = data.check_shipping_availability_text

                if (data.check_shipping_availability.toString().toDouble() > 0) {
                    available.setTextColor(ContextCompat.getColor(context, R.color.black))
                } else {
                    available.setTextColor(ContextCompat.getColor(context, R.color.red))
                }


                sale.isVisible =
                    (data.offer_name == "Flash Sale" || data.offer_name == "Shocking Sale" && !data.end_time.isNullOrEmpty())
                if (sale.isVisible) {
                    setTimer(data.end_time, sale, data)
                    sale.text = data.currentEndTime

                }

                if (!data.variants_list.isNullOrEmpty()) {
                    variant.text = data.attr_name1
                }

                soldOut.isVisible = data.is_out_of_stock == 1
                if (data.is_out_of_stock == 1) {
                    checkBox.makeInvisible()
                    checkBox.isEnabled = false
                } else {
                    checkBox.isEnabled = true
                    checkBox.makeVisible()
                }

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
                    if (data.cart_selected.toString().toDouble() > 0 || data.is_out_of_stock > 0) {
                        onDeleteClick?.let {
                            it(data.cart_id.toString())
                        }
                    } else {
                        Toast.makeText(context, "Please select the product", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                variant.setOnClickListener {
                    data.variants_list?.forEach {
                        if (data.attr_name1.toString().contains(it.combination)) {
                            it.isSelected = true
                        }
                    }
                    data.variants_list?.let { onVariantClick?.invoke(it, data.cart_id.toString()) }
                }


            }
        }
    }

    private fun setTimer(endTime: String, sale: TextView, data: Product) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val endParsedTime = dateFormat.parse(endTime) ?: Date()
        val currentTime = Date()
        val timeDifference = endParsedTime.time - currentTime.time
        countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                activity.runOnUiThread {
                    updateCountdownText(millisUntilFinished, sale, data)
                }
            }

            override fun onFinish() {
                activity.runOnUiThread {
                    updateCountdownText(0, sale, data)
                }
            }
        }

        countdownTimer?.start()
    }

    private fun updateCountdownText(millisUntilFinished: Long, sale: TextView, data: Product) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

        val newCountdownText = "Flash Deals Ends In: ${
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d:%02d",
                days,
                hours,
                minutes,
                seconds
            )
        }"

        if (newCountdownText != data.currentEndTime) {
            data.currentEndTime = newCountdownText
            sale.text = newCountdownText
        }/*
        data.currentEndTime = newCountdownText
        sale.text = newCountdownText*/
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

    override fun getItemViewType(position: Int): Int = position

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

    fun setActivity(activity: Activity) {
        this.activity = activity
    }
}