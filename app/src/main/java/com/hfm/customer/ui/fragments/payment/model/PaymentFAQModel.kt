package com.hfm.customer.ui.fragments.payment.model

data class PaymentFAQModel(
    val `data`: PaymentFAQData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class PaymentFAQData(
    val faq: List<Faq>
)
data class Faq(
    val answer: String,
    val id: Int,
    val question: String
)