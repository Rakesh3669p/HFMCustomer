package com.hfm.customer.ui.fragments.myOrders.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class MyOrdersModel(
    val `data`: MyOrdersData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class MyOrdersData(
    val purchase: List<Purchase>,
    val total_orders: Any
)

data class Purchase(
    val auction_status: String,
    val bid_charge: Any,
    val cancel_order_detail: CancelledOrders,
    val cust_message: String,
    val delivery_charges: Double,
    val shippingDiscount: Double,
    val delivery_partner: String,
    val delivery_proof: String,
    val delivery_status: String,
    val delivered_date: String,
    val discount: Double,
    val grand_total: Double,
    val order_date: String,
    val order_id: String,
    val order_status: String,
    val invoice: String,
    val frontend_order_status: String?,
    val order_time: String,
    val order_type: String,
    val payment_mode: String,
    val payment_status: String,
    val payment_upload_status: Int,
    val reject_remarks: String,
    val payment_uploaded_image: String,
    val platform_voucher_amount: Any,
    val products: List<Product>,
    val sale_id: Any,
    val chat_id: String,
    val seller_id: Any,
    val seller_voucher_amount: Double,
    val shipping_address: ShippingAddress,
    val sold_by: String,
    val store_logo: String,
    val store_name: String,
    val sub_total: Double,
    val track_order: String,
    val wallet_amount: Any
)

data class ShippingAddress(
    val address1: String,
    val address2: String,
    val address_type: String,
    val city: String,
    val country: String,
    val email: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val phone: String,
    val state: String,
    val zip_code: String
)

data class CancelledOrders(
    val cancel_id: Int,
    val cancel_title: String,
    val cancel_notes: String?,
    val canceled_date: String
)