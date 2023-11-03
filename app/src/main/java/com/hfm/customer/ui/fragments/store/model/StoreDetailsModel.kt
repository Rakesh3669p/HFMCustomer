package com.hfm.customer.ui.fragments.store.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product
import com.hfm.customer.ui.fragments.products.productList.model.SubcategoryData

data class StoreDetailsModel(
    val `data`: StoreData,
    val httpcode: Int,
    val status: String
)

data class StoreData(
    val best_products: List<Any>,
    var product: List<Product>,
    val shop_detail: List<ShopDetail>,
    val subcategory_data: SubcategoryData,
    val total_products: Int
)


data class ShopDetail(
    val about: String,
    val address_line1: String,
    val address_line2: Any,
    val banner: String,
    val banners: List<String>,
    val chat_id:String,
    val categories: List<Category>,
    val city: String,
    val contact_num: String,
    val country: String,
    val followers: Int,
    var is_following: Int,
    val join_date: String,
    val logo: String,
    val no_of_products: Int,
    val postive_review: Any,
    val promotion_image: String,
    val promotion_link: String,
    val seller_id: Int,
    val service_status: Int,
    val state: String,
    val store_details: String,
    val store_id: Int,
    val store_name: String,
    val store_prd_rating: Int,
    val store_rating: Int,
    val total_orders: Int,
    val video: String
)

/*data class SubcategoryData(
    val subcategory: List<Any>
)*/


data class Category(
    val id: Int,
    val name: String
)