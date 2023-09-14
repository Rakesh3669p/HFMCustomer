package com.hfm.customer.ui.dashBoard.home.model

data class FeatureProductsModel(
    val `data`: FeatureProductsData,
    val httpcode: Any,
    val status: String
)

data class FeatureProductsData(
    val products: List<Product>,
    val total_products: Any
)

data class Product(
    val actual_price: Any,
    val brand_id: Any,
    val brand_name: String,
    val category_id: Any,
    val category_name: String,
    val image: List<Image>,
    val offers: List<Offer>,
    val product_id: Any,
    val wholesale: Int,
    val frozen: Int,
    val offer: String,
    val product_name: String,
    val product_type: String,
    val rating: Any,
    val sale_price: Boolean,
    val seller: String,
    val seller_id: Any,
    val service_status: Any,
    val shock_sale_price: Boolean,
    val short_description: String,
    val special_ofr_price: Boolean,
    val subcategory_id: Any,
    val subcategory_name: String,
    val tag: List<Any>,
    val total_reviews: Any
)

data class Image(
    val image: String,
    val thumbnail: String
)

data class Offer(
    val offer_name: String,
    val product_id: Any,
    val url: String
)