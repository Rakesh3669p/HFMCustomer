package com.hfm.customer.ui.fragments.products.productDetails.model

class SellerVoucherModel : ArrayList<SellerVoucherModelItem>()

data class SellerVoucherModelItem(
    val coupon_code: String,
    val coupon_id: Int,
    val desc: String,
    val minimum_purchase: Int,
    val offer: String,
    val offer_type: String,
    val offer_value: Any,
    val offer_value_in: String,
    val previous_order_amount: String,
    val previous_order_count: Int,
    val purchase_type: String,
    val shipping_type: Any,
    val title: String,
    val valid_upto: String,
    val voucher_type: Any,
    val status:String="",
)