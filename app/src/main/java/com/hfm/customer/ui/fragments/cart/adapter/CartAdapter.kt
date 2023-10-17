package com.hfm.customer.ui.fragments.cart.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemCartListBinding
import com.hfm.customer.ui.fragments.cart.model.Coupon
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject


class CartAdapter @Inject constructor() : RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    private lateinit var couponData: Coupon
    private lateinit var context: Context
    private var selectAll = false
    private var selectedSellerIds: MutableList<Int> = ArrayList()

    var  couponPosition = -1

    inner class ViewHolder(private val bind: ItemCartListBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun bind(data: SellerProduct) {
            with(bind) {

                voucherDetailsLayout.isVisible = couponPosition == absoluteAdapterPosition
                if(data.seller.coupon!=null){
                    voucher.text = data.seller.coupon.offer


                    if (data.seller.coupon.offer_value_in == "percentage") {
                        // Calculate the discount based on the percentage
                        val storeTotal = data.seller.products.filter { it.cart_selected.toString().toDouble()>0 }.sumOf { it.total_discount_price }
                        val percentageDiscount = (data.seller.coupon.offer_value.toDouble() / 100) * storeTotal
                        val finalAmount = formatToTwoDecimalPlaces(storeTotal - percentageDiscount)
                        onAppliedCoupon?.invoke(true,formatToTwoDecimalPlaces(percentageDiscount))

                        /*// Update UI with the discount
                        binding.platformVoucher.text = "- RM ${formatToTwoDecimalPlaces(percentageDiscount)}"
                        binding.totalAmount.text = "RM $finalAmount"*/

                        voucherDescription.text = "You saved additional RM ${formatToTwoDecimalPlaces(percentageDiscount)}"
                        voucherAmount.text = formatToTwoDecimalPlaces(percentageDiscount)
                    } else {

                        val storeTotal = data.seller.products.filter { it.cart_selected.toString().toDouble()>0 }.sumOf { it.total_discount_price }
                        val finalAmount = formatToTwoDecimalPlaces(storeTotal  - data.seller.coupon.offer_value.toDouble())

                        // Update UI with the discount
                        /*binding.platformVoucher.text = "- RM ${formatToTwoDecimalPlaces(coupon.offer_value.toDouble())}"
                        binding.totalAmount.text = "RM $finalAmount"*/

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
                checkBoxStore.text = data.seller.seller
                val productAdapter = CartProductAdapter()
                initRecyclerView(context, productsRv, productAdapter)
                productAdapter.differ.submitList(data.seller.products)

                productAdapter.setOnDeleteClickListener { cartId ->
                    onDeleteClick?.invoke(cartId)
                }

                productAdapter.setOnQtyChangeListener { cartId, qty ->
                    onQtyChange?.invoke(cartId, qty)
                }
                delivery.isVisible = false
//                delivery.isVisible = data.seller.cart_selected == 1 &&  data.shipping_availability_text != "available"
//                delivery.text = data.shipping_availability_text
                val shippingCharge = data.shipping.toString().toDouble()
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

                productAdapter.setOnProductSelection { cartId, status ->
                    onCartSelection?.invoke(cartId, status)
                }

                productAdapter.setOnVariantClick { variants, cartId ->
                    onVariantClick?.invoke(variants, cartId)
                }

                if (data.seller.cart_selected == 1 || selectAll) {
                    checkBoxStore.isChecked = true
                }

                checkBoxStore.setOnCheckedChangeListener { _, isChecked ->
                    var cartIds = ""
                    data.seller.products.forEach {
                        cartIds = "$cartIds,${it.cart_id}"
                    }
                    onCartSelection?.invoke(cartIds, isChecked)
                }
                productAdapter.selectAllProducts(selectAll)
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
    ): CartAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCartListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCartSelection: ((cartId: String, status: Boolean) -> Unit)? = null

    fun setOnCartSelectionClickListener(listener: (cartId: String, status: Boolean) -> Unit) {
        onCartSelection = listener
    }

    private var onDeleteClick: ((id: String) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (id: String) -> Unit) {
        onDeleteClick = listener
    }

    private var onQtyChange: ((id: String, qty: String) -> Unit)? = null

    fun setOnQtyChangeListener(listener: (id: String, qty: String) -> Unit) {
        onQtyChange = listener
    }

    private var onShopVoucherClick: ((id: String) -> Unit)? = null

    fun setOnShopVoucherClickListener(listener: (id: String) -> Unit) {
        onShopVoucherClick = listener
    }

    private var onVariantClick: ((variants: List<Variants>, cartId: String) -> Unit)? = null

    fun setOnVariantClick(listener: (variants: List<Variants>, cartId: String) -> Unit) {
        onVariantClick = listener
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
}