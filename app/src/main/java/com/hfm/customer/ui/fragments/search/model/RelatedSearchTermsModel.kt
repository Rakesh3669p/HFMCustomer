package com.hfm.customer.ui.fragments.search.model

data class RelatedSearchTermsModel(
    val `data`: RelatedTermsData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class RelatedTermsData(
    val related_terms: List<RelatedTerm>
)

data class RelatedTerm(
    val name: String
)