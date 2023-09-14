package com.hfm.customer.ui.dashBoard.home.model

data class HomeFlashSaleCategory(
    val data: FlashSaleCatData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class FlashSaleCatData(val categories: List<Category>)

data class Category(
    val category_id: Int,
    val category_image: String,
    val category_name: String
)