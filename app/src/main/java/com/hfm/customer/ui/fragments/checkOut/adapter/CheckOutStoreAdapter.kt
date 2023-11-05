package com.hfm.customer.ui.fragments.checkOut.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.checkOut.model.ShippingOption
import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject


class CheckOutStoreAdapter @Inject constructor() :
    RecyclerView.Adapter<CheckOutStoreAdapter.ViewHolder>() {
    private var shippingOptions: List<ShippingOption> = ArrayList()
    private lateinit var context: Context
    private var initialShippingSetupDone = false

    inner class ViewHolder(private val bind: ItemCheckoutListBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: SellerProduct) {
            with(bind) {

                val red = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                val greyLite = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_lite))
                val greyDark = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textGreyDark))
                val white = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                val hasStandardDelivery = shippingOptions.any { it.title == "Standard Delivery" && it.is_active == 1 }
                val hasSelfPickup = shippingOptions.any { it.title == "Self Pickup" && it.is_active == 1 }

                if (absoluteAdapterPosition<=differ.currentList.size-1) {
                    data.standardPickUp = hasStandardDelivery
                    data.selfPickUp = hasStandardDelivery && !hasSelfPickup
                }

                standardDelivery.isVisible = hasStandardDelivery
                selfPickup.isVisible = hasSelfPickup

                if (data.selfPickUp) {
                    selfPickup.backgroundTintList = red
                    selfPickup.setTextColor(white)
                } else {
                    selfPickup.backgroundTintList = greyLite
                    selfPickup.setTextColor(greyDark)
                }

                if (data.standardPickUp) {
                    standardDelivery.backgroundTintList = red
                    standardDelivery.setTextColor(white)
                } else {
                    standardDelivery.backgroundTintList = greyLite
                    standardDelivery.setTextColor(greyDark)
                }

                standardDelivery.setOnClickListener {
                    data.selfPickUp = false
                    data.standardPickUp = true
                    onShippingOption?.invoke(data.seller.seller_id,shippingOptions[0].id)
                    notifyDataSetChanged()
                }

                selfPickup.setOnClickListener {
                    data.selfPickUp = true
                    data.standardPickUp = false
                    onShippingOption?.invoke(data.seller.seller_id,shippingOptions[1].id)
                    notifyDataSetChanged()
                }

                voucherDetailsLayout.isVisible = data.is_seller_coupon_applied == 1
                if (data.is_seller_coupon_applied == 1) {
                    voucher.text = data.seller_coupon_data.title

                    val message =
                        "You saved additional RM " + formatToTwoDecimalPlaces(data.seller_coupon_data.seller_coupon_discount_amt)


                    val spannableString = SpannableString(message)
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    val startIndex =
                        message.indexOf(formatToTwoDecimalPlaces(data.seller_coupon_data.seller_coupon_discount_amt))
                    val endIndex: Int =
                        startIndex + formatToTwoDecimalPlaces(data.seller_coupon_data.seller_coupon_discount_amt).length
                    spannableString.setSpan(
                        boldSpan,
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    voucherDescription.text = spannableString
//                    voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(data.seller_coupon_data.seller_coupon_discount_amt)}"
                }
                storeName.text = data.seller.seller
                removeCoupon.setOnClickListener {
                    onRemoveCoupon?.invoke(data.seller_coupon_data.coupon_id)
                }

                val productAdapter = CheckOutProductAdapter()
                initRecyclerView(context, productsRv, productAdapter)

                val checkOutProducts: MutableList<Product> = ArrayList()
                val checkOutStore =
                    data.seller.products.filter { it.cart_selected.toString().toDouble() > 0 }
                checkOutProducts.addAll(checkOutStore)
                productAdapter.differ.submitList(checkOutProducts)

                val shippingCharge = (if(data.shipping==null) 0 else data.shipping.toString().toDouble()).toDouble()
                shippingCharges.isVisible = shippingCharge > 0
                val formattedShipping =
                    "Shipping Charges: RM ${formatToTwoDecimalPlaces(shippingCharge)}"
                val spannableString = SpannableString(formattedShipping)

                spannableString.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    16,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val startIndex = 17
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

                storeName.setOnClickListener {
                    onStoreClicked?.invoke(data.seller.seller_id)
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


    private var onMessage: ((message: String, position: Int) -> Unit)? = null

    fun setOnMessageListener(listener: (message: String, position: Int) -> Unit) {
        onMessage = listener
    }

    private var onShopVoucherClick: ((id: String) -> Unit)? = null

    fun setOnShopVoucherClickListener(listener: (id: String) -> Unit) {
        onShopVoucherClick = listener
    }


    private var onAppliedCoupon: ((status: Boolean, amount: String) -> Unit)? = null

    fun setOnAppliedCoupon(listener: (status: Boolean, amount: String) -> Unit) {
        onAppliedCoupon = listener
    }

    private var onRemoveCoupon: ((id: Int) -> Unit)? = null

    fun setOnSellerRemoveCoupon(listener: (id: Int) -> Unit) {
        onRemoveCoupon = listener
    }

    private var onShippingOption: ((sellerId: Int,shippingId:Int) -> Unit)? = null

    fun setOnShippingOption(listener: (sellerId: Int,shippingId:Int) -> Unit) {
        onShippingOption = listener
    }


    fun setShippingOptions(shippingOptions: List<ShippingOption>) {
        this.shippingOptions = shippingOptions
    }

    private var onStoreClicked: ((id: Int) -> Unit)? = null

    fun setOnStoreClicked(listener: (id: Int) -> Unit) {
        onStoreClicked = listener
    }
}