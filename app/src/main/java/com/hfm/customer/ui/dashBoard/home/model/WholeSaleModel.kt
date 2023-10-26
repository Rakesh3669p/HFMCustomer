package com.hfm.customer.ui.dashBoard.home.model

data class WholeSaleModel(
    val data: WholeSaleData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class WholeSaleData(
    val flash_sale: WholeSale,
    val total_products: Int
)

data class WholeSale(
    val products: List<WholeSaleProduct>
)

data class WholeSaleProduct(
    val actual_price: Any,
    val brand: Boolean,
    val category: Any,
    val image: List<Image>,
    val offer: String,
    val offer_price: Any,
    val wholesale: Any,
    val frozen: Any,
    val product_id: Int,
    val product_name: String,
    val product_type: String,
    val rating: Any,
    val sale_price: Any,
    val seller: String,
    val service_status: Int,
    val special_ofr_price: Boolean,
    val subcategory: Boolean,
    val total_reviews: Any
)