package com.hfm.customer.ui.fragments.support.model

data class SupportTicketsModel(
    val `data`: SupportTicketData,
    val httpcode: Int,
    val message: String,
    val status: String
)
data class SupportTicketData(
    val count: Int,
    val list: List<SupportTickets>
)


data class SupportTickets(
    val created_at: String,
    val subject: String,
    val last_message: String,
    val support_id: Int,
    val ticket_id: String
)