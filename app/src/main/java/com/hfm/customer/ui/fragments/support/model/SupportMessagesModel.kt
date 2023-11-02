package com.hfm.customer.ui.fragments.support.model

import com.hfm.customer.ui.fragments.myOrders.model.Purchase

data class SupportMessagesModel(
    val `data`: SupportMessagesData,
    val httpcode: Int,
    val message: String,
    val status: String
)
data class SupportMessagesData(
    val support_messages: SupportMessages
)
data class SupportMessages(
    val created_at: String,
    val messages: List<Message>,
    val order_details: List<Purchase>,
    val subject: String,
    val support_id: Int,
    val ticket_id: String
)

data class Message(
    val align: String,
    val created_at: String,
    val from: String,
    val image: String,
    val me: Int,
    val message: String,
    var warningMessage: Boolean,
    var warning: Boolean,
    val read_status: Int
)