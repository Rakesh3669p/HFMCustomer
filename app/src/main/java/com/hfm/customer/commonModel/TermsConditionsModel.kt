package com.hfm.customer.commonModel

data class TermsConditionsModel(
    val `data`: TermsConditionsData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class TermsConditionsData(
    val terms_conditions: TermsConditions
)

data class TermsConditions(
    val business_customer_terms: String,
    val customer_register: String,
    val goods_tc: String
)