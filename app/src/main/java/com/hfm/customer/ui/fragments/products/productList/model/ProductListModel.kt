package com.hfm.customer.ui.fragments.products.productList.model

import com.hfm.customer.ui.fragments.products.productDetails.model.Product

data class ProductListModel(
    val data: ProductListData,
    val httpcode: Int,
    val page: String,
    val status: String,
    val message: String
)

data class ProductListData(
    val currency: String,
    val products: List<Product>,
    val subcategory_data: SubcategoryData,
    val total_products: Int
)

data class SubcategoryData(
    val category_breadcrumbs: Any,
    val subcategory: List<Subcategory>?=null
)

data class CategoryBreadcrumbs(
    val category: Category
)

data class Category(
    val category_id: Int,
    val category_name: String
)

data class Subcategory(
    val id: Int,
    val subcategory_name: String
)

