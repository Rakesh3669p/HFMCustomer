package com.hfm.customer.commonModel

data class AppUpdateModel(
    val `data`: UpdateData,
    val message: String,
    val status: Boolean
)

data class UpdateData(
    val created_at: String,
    val critical_update: Int,
    val critical_update_message: String,
    val deleted_at: Any,
    val id: Int,
    val is_active: Int,
    val normal_update_message: String,
    val platform: String,
    val test_version: Int,
    val updated_at: String,
    val version: String,
    val version_code: Int,
    val version_info: String
)