package com.hfm.customer.ui.dashBoard.home.model

data class HomeBrandsModel(
    val `data`: BrandsData,
    val httpcode: Int,
    val message: String,
    val status: String
)
data class BrandsData(
    val brands: List<Brand>
)

data class Brand(
    val brand_description: String,
    val brand_id: Int,
    val brand_image: String,
    val brand_name: String
)