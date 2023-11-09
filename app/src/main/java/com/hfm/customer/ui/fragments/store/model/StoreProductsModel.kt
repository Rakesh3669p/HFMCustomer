package com.hfm.customer.ui.fragments.store.model

import com.hfm.customer.ui.dashBoard.home.model.Image
import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class StoreProductsModel(
    val `data`: StoreProductData,
    val httpcode: Int,
    val status: String
)

data class StoreProductData(
    val product: List<Product>,
    val total_products: Int
)

data class StoreProduct(
    val actual_price: Int,
    val actual_price_quote: String,
    val brand_id: Int,
    val brand_name: String,
    val category_id: Int,
    val category_name: String,
    val chilled: Int,
    val content: String,
    val frozen: Int,
    val image: List<Image>,
    val is_out_of_stock: Int,
    val long_description: String,
    val product_id: Int,
    val product_name: String,
    val rating: Int,
    val sale_price: Int,
    val seller: String,
    val seller_id: Int,
    val service_status: Int,
    val short_description: String,
    val stock: Int,
    val subcategory_id: Int,
    val tag: List<Any>,
    val total_reviews: Int,
    val variants_list: List<Variants>,
    val wholesale: Int
)

data class Variants(
    val actual_price: Int,
    val bulk_order_qty: String,
    val combination: String,
    val discount_type: String,
    val image: String,
    val is_out_of_stock: Int,
    val min_order_qty: String,
    val offer: String,
    val offer_name: String,
    val offer_price: Any,
    val out_of_stock_selling: Int,
    val pro_id: Int,
    val stock: Int
)