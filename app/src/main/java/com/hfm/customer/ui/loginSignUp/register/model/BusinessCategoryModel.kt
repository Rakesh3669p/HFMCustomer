package com.hfm.customer.ui.loginSignUp.register.model

data class BusinessCategoryModel(
    val `data`: BusinessData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BusinessData(
    val business_categoryies: List<BusinessCategoryy>
)

data class BusinessCategoryy(
    val created_at: String,
    val created_by: Any,
    val crm_id: Int,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val modified_by: Int,
    val name: String,
    val updated_at: String
)