package com.hfm.customer.ui.fragments.myOrders.model

import com.hfm.customer.ui.dashBoard.home.model.Image

data class BulkOrdersListModel(
    val `data`: BulkOrdersData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BulkOrdersData(
    val bulkrequest_order_details: List<BulkrequestOrderDetail>,
    val total_orders: Int
)

data class BulkrequestOrderDetail(
    val bulkrequest_id: Int,
    val bulkrequest_order_id: String,
    val contact_no: String,
    val currency: String,
    val customer_name: String,
    val date_needed: String,
    val email: String,
    val hfm_margin: Any,
    val order_id: String,
    val product_id: Int,
    val product_image: List<Image>,
    val product_name: String,
    val product_type: String,
    val quantity: Int,
    val quotation_status: Int,
    val remarks: String,
    val request_date: String,
    val request_status: Int,
    val request_time: String,
    val sale_id: String?,
    val sale_price: Any,
    val shipping_address: String,
    val shipping_charges: Any,
    val unit_of_measure: String,
    val unit_price: Any
)