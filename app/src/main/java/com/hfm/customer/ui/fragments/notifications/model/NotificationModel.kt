package com.hfm.customer.ui.fragments.notifications.model

data class NotificationModel(
    val `data`: NotificationData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class NotificationData(
    val notification_count: Int,
    val notifications: List<Notification>,
    val unread_count: Int
)

data class Notification(
    val id: Int,
    val title: String,
    val notify_type: String,
    val app_target_page: String,
    val order_id: String,
    val ref_id: Int,
    val bulk_order_sale_id: Int,
    val description: String,
    val ref_link: String,
    val viewed: Int,
    val created_at: String
)