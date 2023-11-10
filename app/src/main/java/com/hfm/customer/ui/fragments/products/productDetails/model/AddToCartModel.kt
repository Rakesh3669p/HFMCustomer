package com.hfm.customer.ui.fragments.products.productDetails.model

data class AddToCartModel (
    val httpcode: Int = 0,
    val success: String,
    val message: String,
    val status:String,
    val data:AddToCardData,
    val error:List<String>)

data class AddToCardData(
    val cart_id:Int,
    val cart_count:Int
)