package com.hfm.customer.ui.fragments.products.productDetails.model

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
    val sidebar_section: List<String>,
    val varaiants_list: List<Any>
)

data class CategoryBreadcrumbs(
    val category_breadcrumbs: CategoryBreadcrumbsX
)
data class CategoryBreadcrumbsX(
    val category: Category,
    val subcategory: Subcategory
)

data class Category(
    val category_id: Int,
    val category_name: String
)

data class Subcategory(
    val id: Int,
    val level: Int,
    val subcategory_name: String
)

data class CrossSellingProduct(
    val actual_price: Any,
    val brand_id: Any,
    val brand_name: String,
    val category_id: Any,
    val category_name: String,
    val content: String,
    val discount_type: Boolean,
    val image: List<Image>,
    val is_out_of_stock: Any,
    val long_description: String,
    val offer: Boolean,
    val offer_name: Boolean,
    val offer_price: Boolean,
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
    val actual_price: Int,
    val discount_type: Boolean,
    val image: List<Image>,
    val offer: Boolean,
    val offer_name: Boolean,
    val offer_price: Any,
    val product_id: Int,
    val product_name: String,
    val product_type: String
)

data class Product(
    val actual_price: Any,
    val attrs_list: List<Any>,
    val brand_id: Any,
    val brand_name: String,
    val bulk_quantity: Any,
    val category_id: Any,
    val category_name: String,
    val content: String,
    val discount_type: Boolean,
    val image: List<Image>,
    val in_wishlist: Any,
    val is_featured: Any,
    val is_out_of_stock: Boolean,
    val long_description: String,
    val minimum_quantity: Any,
    val offer: Any,
    val offer_name: Any,
    val offer_price: Any,
    val out_of_stock_selling: Boolean,
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
    val video: String
)
data class Image(
    val image: String,
    val thumbnail: String
)

data class SellerInfo(
    val banner: String,
    val join_date: String,
    val logo: String,
    val no_of_products: Int,
    val seller_id: Int,
    val seller_rating_count: Int,
    val store_id: Int,
    val store_name: String,
    val store_prd_rating: Int,
    val store_rating: Int
)