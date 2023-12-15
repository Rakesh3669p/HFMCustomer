package com.hfm.customer.ui.fragments.cart.model

import com.google.gson.annotations.SerializedName

data class CouponAppliedModel(
    val data: CouponData,
    val httpcode: Int,
    val message: String,
    val status: String
)
data class CouponData(
    val coupon: Coupon
)

data class Coupon(
    @SerializedName("coupon_code") val couponCode: String,
    @SerializedName("coupon_id") val couponId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("desc") val desc: String,
    @SerializedName("store_logo") val storeLogo: String,
    @SerializedName("is_claimed") val isClaimed: String,
    @SerializedName("minimum_purchase") val minimumPurchase: Any,
    @SerializedName("offer") val offer: String,
    @SerializedName("offer_type") val offerType: String,
    @SerializedName("offer_value") val offerValue: Double,
    @SerializedName("offer_value_cal") val offerValueCal: Double,
    @SerializedName("offer_value_in") val offerValueIn: String,
    @SerializedName("previous_order_amount") val previousOrderAmount: String,
    @SerializedName("previous_order_count") val previousOrderCount: String,
    @SerializedName("purchase_type") val purchaseType: String,
    @SerializedName("shipping_voucher_type") val shippingVoucherType: String,
    @SerializedName("title") val title: String,
    @SerializedName("valid_upto") val validUpto: String,
    @SerializedName("used_on") val usedOn: String,
    @SerializedName("voucher_type") val voucherType: String,

)

data class CouponApplied(
    @SerializedName ("coupon_id") val coupon_id:Int,
    @SerializedName("title") val title:String,
    @SerializedName("coupon_type") val coupon_type:String,
    @SerializedName("ofr_type") val ofr_type:String,
    @SerializedName("ofr_amount") val ofr_amount:Int,
    @SerializedName("discount_type") val discount_type:String,
    @SerializedName("platform_discount_type") val platform_discount_type:String,
    @SerializedName("platform_discount_amount") val platform_discount_amount:Double,
    @SerializedName("voucher_type") val voucher_type:String,
    @SerializedName("is_free_shipping") val is_free_shipping:Int,
    @SerializedName("seller_subtotal") val seller_subtotal:Double,
    @SerializedName("seller_coupon_discount_amt") val seller_coupon_discount_amt:Double,
    @SerializedName("seller_total_cost") val seller_total_cost:Double,
)