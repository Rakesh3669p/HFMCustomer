package com.hfm.customer.ui.dashBoard.chat.model

data class ChatListModel(
    val `data`: ChatData,
    val httpcode: Int,
    val message: String,
    val status: String
)


data class ChatData(
    val order_count: Int,
    val list: List<ChatList>
)

data class ChatList(
    val chat_id: Int,
    val logo: String,
    val order_id: String,
    val sale_id: Long,
    val seller_id: String?="",
    val seller_message: String,
    val seller_name: String,
    val store_name: String,
    val unread_msg: Int,
    val last_message:String,
    val last_chat_date:String,
    val seller_vaccation:String
)