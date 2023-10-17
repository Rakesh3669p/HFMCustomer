package com.hfm.customer.ui.fragments.cart.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class CartModel(
    val data: CartData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class CartData(
    val before_checkout_products: List<Any>,
    val cart_count: Int,
    val currency: String,
    val discount: Any,
    val grand_total: Any,
    val points: String,
    val reward: List<Any>,
    val selected_products_count: Int,
    val seller_product: List<SellerProduct>,
    val shipping_charges: Any,
    val shipping_customer_type: String,
    val total_cost: Any,
    val total_offer_cost: Any,
    val total_tax: Any,
    val voucher_remaining: Any,
    val wallet_balance: String
)

data class SellerProduct(
    val seller: Seller,
    val shipping: Any,
    val shipping_availability: Int,
    val shipping_availability_text: String,
    val shipping_cal: Any,
    val shipping_markup: Any,
    val shipping_method: String
)

data class Seller(
    var cart_selected: Int,
    val products: List<Product>,
    val seller: String,
    var coupon: Coupon,
    val seller_id: Int,
    var message: String?="",
    val service_status: Int
)