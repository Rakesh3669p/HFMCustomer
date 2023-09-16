package com.hfm.customer.ui.fragments.wishlist.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class WishListModel(
    val `data`: WishListData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class WishListData(
    val count_wish: Int,
    val wishlist: List<Product>
)