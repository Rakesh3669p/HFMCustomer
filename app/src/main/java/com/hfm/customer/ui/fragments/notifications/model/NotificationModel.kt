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
    val created_at: String,
    val description: String,
    val id: Int,
    val notify_type: String,
    val ref_id: Int,
    val ref_link: String,
    val title: String,
    val viewed: Int
)