package com.hfm.customer.ui.dashBoard.home.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class FlashSaleModel(
    val `data`: FlashSaleData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class FlashSaleData(
    val flash_sale: FlashSale,
    val total_products: Int
)

data class FlashSale(
    val end_time: String,
    val products: List<Product>,
    val shock_sale_id: Int,
    val shock_sale_title: String,
    val start_time: String
)