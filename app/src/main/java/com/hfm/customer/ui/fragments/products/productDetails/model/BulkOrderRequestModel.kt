package com.hfm.customer.ui.fragments.products.productDetails.model

data class BulkOrderRequestModel(
    val `data`: BulkOrderRequestData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BulkOrderRequestData(
    val bulk_request_id: Int,
    val bulkrequest_order_id: String
)