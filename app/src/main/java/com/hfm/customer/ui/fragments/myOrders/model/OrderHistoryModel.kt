package com.hfm.customer.ui.fragments.myOrders.model

data class OrderHistoryModel(
    val `data`: OrderHistoryData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class OrderHistoryData(
    val order: List<Order>
)

data class Order(
    val available: Int?=0,
    val date: String,
    val description: String,
    val identifier: String,
    val timestamp: String,
    val title: String
)

