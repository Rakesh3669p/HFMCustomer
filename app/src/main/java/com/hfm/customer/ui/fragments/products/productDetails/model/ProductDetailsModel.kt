package com.hfm.customer.ui.fragments.products.productDetails.model

import java.util.Locale.IsoCountryCode

data class ProductDetailsModel(
    val data: ProductData,
    val httpcode: Int,
    val status: String,
    val message: String
)

data class ProductData(
    val category_breadcrumbs: CategoryBreadcrumbs,
    val cross_selling_products: List<CrossSellingProduct>,
    val other_products: List<OtherProduct>,
    val product: Product,
    val relative_products: List<Any>,
    val seller_info: List<SellerInfo>,
    val customer_addr: CustomerAddress?,
    val sidebar_section: List<String>,
    val varaiants_list: List<Variants>
)

data class CustomerAddress(
    val country_code: String,
    val country_id: String,
    val pincode:String?,
)

data class Variants(
    val pro_id: Any,
    val combination: String,
    val stock: Any,
    val is_out_of_stock: Any,
    val out_of_stock_selling: String,
    val min_order_qty: Any,
    val bulk_order_qty: Any,
    val image: String,
    val offer_name: Any,
    val discount_type: Any,
    val offer: Any,
    var isSelected: Boolean = false,
    val actual_price: Double,
    val offer_price: String?
)

data class CategoryBreadcrumbs(
    val category_breadcrumbs: CategoryBreadcrumbsX
)

data class CategoryBreadcrumbsX(
    val category: Category,
    val subcategory: Any
)

data class Category(
    val category_id: Any,
    val category_name: String
)

data class Subcategory(
    val id: Any,
    val level: Any,
    val subcategory_name: String
)

data class CrossSellingProduct(
    val actual_price: Double,
    val brand_id: Any,
    val brand_name: String,
    val category_id: Any,
    val category_name: String,
    val content: String,
    val discount_type: Boolean,
    val image: List<Image>,
    val is_out_of_stock: Int,
    val long_description: String,
    val offer: Any,
    val offer_name: Any,
    val offer_price: Double,
    val product_id: Any,
    val product_name: String,
    val product_type: String,
    val rating: Any,
    val short_description: String,
    val subcategory_id: Any,
    val subcategory_name: String,
    val tag: List<Any>,
    val total_reviews: Any,
    val variants_list: List<Any>
)

data class OtherProduct(
    val actual_price: Any,
    val discount_type: Boolean,
    val image: List<Image>,
    val offer: Boolean,
    val offer_name: Boolean,
    val offer_price: Any,
    val product_id: Any,
    val product_name: String,
    val product_type: String
)

data class Product(
    val isPurchased: Int,
    val actual_price: Double,
    val attrs_list: List<Any>,
    val brand_id: Any,
    val brand_name: String,
    val bulk_quantity: Any,
    val category_id: Any,
    val category_name: String,
    val content: String,
    val discount_type: Boolean,
    val image: List<Image>?,
    val product_image: List<Image>?,
    var in_wishlist: Int,
    val is_featured: Any,
    val is_out_of_stock: Int,
    val attr_name1: String,
    val long_description: String,
    val minimum_quantity: Any,
    val offer: String?="",
    val frozen: Int,
    val chilled: Int,
    val wholesale: Double,
    val offer_name: Any,
    val end_time: String,

    val offer_price: Any?,
    val shock_sale_price: Any?,
    val out_of_stock_selling: Any,
    val product_id: Any,
    val product_name: String,
    val product_type: String,
    val rating: Any,
    val seller: String,
    val seller_id: Any,
    val short_description: String,
    val sku: Any,
    val specification: String,
    val stock: Any,
    val subcategory_id: Any,
    val subcategory_name: String,
    val tag: List<Any>,
    val total_reviews: Any,
    val review_submitted: Int,
    val video: String?="",
    val cart_id: Any,
    var cart_selected: Any,
    val check_shipping_availability: Any,
    val check_shipping_availability_text: String,
    val combo_products: List<ComboProducts>,
    val commission: Any,
    val count_of_combo_products: Any,
    val currency: String,
    val offer_available: Any,
    val quantity: Any,
    val total_actual_price: Any,
    val total_discount_price: Double,
    val sale_price: Any?,
    val total_offer_price: Double,
    val total_tax_value: Any,
    val unit_actual_price: Any,
    val unit_discount_price: Double,
    val variants_list: List<Variants>?,
    val weight: String
)

data class ComboProducts(
    val product_id:Int,
    val product_name:String,
    val product_type:String,
    val actual_price:Double,
    val offer_price:Any,
    val is_out_of_stock:Int,
    val individual_discount:String,
    val individual_price:Double,
    val individual_qty:String,
    val actual_price_total:Double,
    val individual_price_total:Double,
)
data class Image(
    val image: String,
    val thumbnail: String
)

data class SellerInfo(
    val banner: String,
    val join_date: String,
    val logo: String,
    val no_of_products: Any,
    val seller_id: Any,
    val chat_id: String?="",
    val seller_rating_count: Any,
    val store_id: Any,
    val store_name: String,
    val postive_review: String,
    val followers: String,
    val store_prd_rating: Any,
    val store_rating: Any
)

