package com.hfm.customer.ui.fragments.cart.model

data class CouponAppliedModel(
    val `data`: CouponData,
    val httpcode: Int,
    val message: String,
    val status: String
)
data class CouponData(
    val coupon: Coupon
)

data class Coupon(
    val coupon_code: String,
    val coupon_id: Int,
    val desc: String,
    val minimum_purchase: Any,
    val offer: String,
    val offer_type: String,
    val offer_value: Int,
    val offer_value_cal: Double,
    val offer_value_in: String,
    val previous_order_amount: String,
    val previous_order_count: Int,
    val purchase_type: String,
    val shipping_voucher_type: String,
    val title: String,
    val valid_upto: String,
    val voucher_type: String
)