package com.hfm.customer.ui.dashBoard.chat.model

data class ChatMessageModel(
    val `data`: MessagesData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class MessagesData(
    val messages: Messages
)

data class Messages(
    val chat_id: Int,
    val logo: String,
    val messages: List<Message>,
    val order_id: String,
    val order_date: String?="",
    val grand_total: String?="",

    val sale_id: Any,
    val seller_id: Int,
    val seller_name: String,
    val store_name: String,
)

data class Message(
    val align: String,
    val chat_date: String,
    val chat_time: String,
    val created_at: String,
    val from: String,
    val image: String,
    val me: Int,
    val message: String,
    val read_status: Int,
    var warningMessage: Boolean = false,
    var warning: Boolean = false,
    )