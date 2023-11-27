package com.hfm.customer.commonModel

data class NotificationViewedModel(
    val httpcode: Int,
    val response: String,
    val status: String,
    val data: NotificationViewData
)

data class NotificationViewData(
    val unread_count:Int?,
    val notification_count:Int,
)
