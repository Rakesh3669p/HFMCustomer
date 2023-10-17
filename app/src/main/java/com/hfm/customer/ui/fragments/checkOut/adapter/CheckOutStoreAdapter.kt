package com.hfm.customer.ui.fragments.checkOut.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemCheckoutListBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.checkOut.model.ShippingOption
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject
import kotlin.math.abs

class CheckOutStoreAdapter @Inject constructor() : RecyclerView.Adapter<CheckOutStoreAdapter.ViewHolder>() {
    private var shippingOptions: List<ShippingOption> =  ArrayList()

    private lateinit var couponData: Coupon
    private lateinit var context: Context

    var  couponPosition = -1

    inner class ViewHolder(private val bind: ItemCheckoutListBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: SellerProduct) {
            with(bind) {


                voucherDetailsLayout.isVisible = couponPosition == absoluteAdapterPosition
                if(data.seller.coupon!=null){
                    voucher.text = data.seller.coupon.offer
                    standardDelivery.isVisible = shippingOptions.any { it.title == "Standard Delivery" && it.is_active == 1 }
                    selfPickup.isVisible = shippingOptions.any { it.title == "Self Pickup" && it.is_active == 1 }

                    if (data.seller.coupon.offer_value_in == "percentage") {
                        val storeTotal = data.seller.products.filter { it.cart_selected.toString().toDouble()>0 }.sumOf { it.total_discount_price }
                        val percentageDiscount = (data.seller.coupon.offer_value.toDouble() / 100) * storeTotal
                        onAppliedCoupon?.invoke(true,formatToTwoDecimalPlaces(percentageDiscount))

                        voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(percentageDiscount)}"
                        voucherAmount.text = formatToTwoDecimalPlaces(percentageDiscount)
                    } else {

                        onAppliedCoupon?.invoke(true,formatToTwoDecimalPlaces(data.seller.coupon.offer_value.toString().toDouble()))
                        voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(data.seller.coupon.offer_value_cal.toDouble()) }"
                        voucherAmount.text = formatToTwoDecimalPlaces(data.seller.coupon.offer_value_cal)
                    }
                }

                removeCoupon.setOnClickListener {
                    val amount = voucherAmount.text.toString().toDouble()
                    onRemoveCoupon?.invoke(amount)
                    voucherDetailsLayout.isVisible = false
                }

                val productAdapter = CheckOutProductAdapter()
                initRecyclerView(context, productsRv, productAdapter)

                val checkOutProducts: MutableList<Product> = ArrayList()
                val checkOutStore = data.seller.products.filter { it.cart_selected.toString().toDouble() > 0 }
                checkOutProducts.addAll(checkOutStore)
                productAdapter.differ.submitList(checkOutProducts)

                val shippingCharge = data.shipping.toString().toDouble()
                shippingCharges.isVisible = shippingCharge > 0
                val formattedShipping = "Shipping Charges: RM ${formatToTwoDecimalPlaces(shippingCharge)}"
                val spannableString = SpannableString(formattedShipping)

                spannableString.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    16,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val startIndex = 16
                val endIndex = formattedShipping.length
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                shippingCharges.text = spannableString

                shopVoucherLbl.setOnClickListener {
                    onShopVoucherClick?.invoke(data.seller.seller_id.toString())
                }

                messageEdt.doOnTextChanged { text, start, before, count ->
                    onMessage?.invoke(text.toString(), absoluteAdapterPosition)
                }
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<SellerProduct>() {
        override fun areItemsTheSame(oldItem: SellerProduct, newItem: SellerProduct): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SellerProduct, newItem: SellerProduct): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtil)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckOutStoreAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCheckoutListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CheckOutStoreAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size


    private var onMessage: ((message: String,position:Int) -> Unit)? = null

    fun setOnMessageListener(listener: (message: String,position:Int) -> Unit) {
        onMessage = listener
    }

private var onShopVoucherClick: ((id: String) -> Unit)? = null

    fun setOnShopVoucherClickListener(listener: (id: String) -> Unit) {
        onShopVoucherClick = listener
    }


    private var onAppliedCoupon: ((status: Boolean,amount:String) -> Unit)? = null

    fun setOnAppliedCoupon(listener: (status: Boolean,amount:String) -> Unit) {
        onAppliedCoupon = listener
    }

    private var onRemoveCoupon: ((amount:Double) -> Unit)? = null

    fun setOnSellerRemoveCoupon(listener: (amount:Double) -> Unit) {
        onRemoveCoupon = listener
    }

    fun setCouponApplied(couponPosition: Int, coupon: Coupon) {
        this.couponPosition = couponPosition
        couponData = coupon
        notifyItemChanged(couponPosition)
    }

    fun setShippingOptions(shippingOptions: List<ShippingOption>) {
        this.shippingOptions = shippingOptions
    }
}