package com.hfm.customer.ui.dashBoard.home.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class FeatureProductsModel(
    val `data`: FeatureProductsData,
    val httpcode: Any,
    val status: String
)

data class FeatureProductsData(
    val products: List<Product>,
    val total_products: Any
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