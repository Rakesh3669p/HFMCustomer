package com.hfm.customer.ui.fragments.cart.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hfm.customer.R
import com.hfm.customer.databinding.ItemCartListBinding
import com.hfm.customer.ui.fragments.cart.model.SellerProduct
import com.hfm.customer.ui.fragments.products.productDetails.model.Variants
import com.hfm.customer.utils.formatToTwoDecimalPlaces
import com.hfm.customer.utils.initRecyclerView
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class CartAdapter @Inject constructor() : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var selectAll = false
    private lateinit var activity:Activity
    inner class ViewHolder(private val bind: ItemCartListBinding) :
        RecyclerView.ViewHolder(bind.root) {

        fun bind(data: SellerProduct) {
            with(bind) {

                voucherDetailsLayout.isVisible = data.is_seller_coupon_applied == 1
                if (data.is_seller_coupon_applied == 1) {
                    voucher.text = data.seller_coupon_data.title
                    voucherDescription.text =
                        "You saved additional RM ${formatToTwoDecimalPlaces(data.seller_coupon_data.seller_coupon_discount_amt)}"
                }


                removeCoupon.setOnClickListener {
                    onRemoveCoupon?.invoke(data.seller_coupon_data.coupon_id)
                }

                storeName.text = data.seller.seller
                val productAdapter = CartProductAdapter()
                initRecyclerView(context, productsRv, productAdapter)
                val animator: RecyclerView.ItemAnimator? = productsRv.itemAnimator
                if (animator is DefaultItemAnimator) {
                    animator.supportsChangeAnimations = false
                    productsRv.itemAnimator = null
                }
                productAdapter.setActivity(activity)
                productAdapter.differ.submitList(data.seller.products)

                productAdapter.setOnDeleteClickListener { cartId ->
                    onDeleteClick?.invoke(cartId)
                }

                productAdapter.setOnQtyChangeListener { cartId, qty ->
                    onQtyChange?.invoke(cartId, qty)
                }

                delivery.isVisible = false
                val shippingCharge = (if (data.shipping == null) 0 else data.shipping.toString()
                    .toDouble()).toDouble()
                shippingCharges.isVisible = shippingCharge > 0
                val formattedShipping =
                    "Shipping Charges: RM ${formatToTwoDecimalPlaces(shippingCharge)}"
                val spannableString = SpannableString(formattedShipping)

                spannableString.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    0,
                    17,
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

                productAdapter.setOnProductSelection { cartId, status ->
                    onCartSelection?.invoke(cartId, status)
                }

                productAdapter.setOnVariantClick { variants, cartId ->
                    onVariantClick?.invoke(variants, cartId)
                }

                if (data.seller.cart_selected == 1 || selectAll) {
                    checkBoxStore.isChecked = true
                }


                storeName.setOnClickListener {
                    onStoreClicked?.invoke(data.seller.seller_id)
                }
                checkBoxStore.setOnCheckedChangeListener { _, isChecked ->
                    var cartIds = ""
                    data.seller.products.forEach {
                        cartIds = "$cartIds,${it.cart_id}"
                    }
                    onCartSelection?.invoke(cartIds, isChecked)
                }
                productAdapter.selectAllProducts(selectAll)

                getVoucher.isVisible = data.seller_coupon_remaining != null && data.seller_coupon_remaining > 0
                val remainingAmount = "RM ${data.seller_coupon_remaining?.let { formatToTwoDecimalPlaces(it) }}"
                getVoucher.text = "Buy $remainingAmount more to enjoy the voucher"
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

    private var onAppliedCoupon: ((status: Boolean, amount: String) -> Unit)? = null

    fun setOnAppliedCoupon(listener: (status: Boolean, amount: String) -> Unit) {
        onAppliedCoupon = listener
    }

    private var onRemoveCoupon: ((id: Int) -> Unit)? = null

    fun setOnSellerRemoveCoupon(listener: (id: Int) -> Unit) {
        onRemoveCoupon = listener
    }

    private var onStoreClicked: ((id: Int) -> Unit)? = null

    fun setOnStoreClicked(listener: (id: Int) -> Unit) {
        onStoreClicked = listener
    }

    fun setActivity(activity: Activity){
        this.activity = activity
    }

}