package com.hfm.customer.ui.fragments.wishlist.model

data class StoreWishlistModel(
    val `data`: StoreFavouriteData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class StoreFavouriteData(
    val count_fav: Int,
    val favourite_list: List<Favourite>
)

data class Favourite(
    val about: Any,
    val banner: String,
    val country: String,
    val is_follow: Int,
    val logo: String,
    val no_of_products: Int,
    val postive_review: Double,
    val seller_id: Int,
    val chat_id: Int?=0,
    val state: String,
    val store_id: Int,
    val store_name: String,
    val store_prd_rating: Int,
    val store_rating: Int
)