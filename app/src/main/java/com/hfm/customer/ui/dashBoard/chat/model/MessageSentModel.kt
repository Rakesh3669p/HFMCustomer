package com.hfm.customer.ui.dashBoard.chat.model

data class MessageSentModel(
    val `data`: MessageData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class MessageData(
    val chat_message: List<Message>,
    val response: String
)