package com.hfm.customer.ui.fragments.payment.model

data class PlaceOrderModel(
    val `data`: PlaceOrderData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class PlaceOrderData(
    val order_id: String,
    val amount: String,
    val payment_url: String,
    val cart_count: Int,
    val link: String,
)