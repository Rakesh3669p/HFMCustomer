package com.hfm.customer.ui.fragments.vouchers.model

import com.hfm.customer.ui.fragments.cart.model.Coupon

data class VoucherListModel(
    val `data`: CouponData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class CouponData(
    val coupon_list: List<Coupon>
)