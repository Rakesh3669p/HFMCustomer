package com.hfm.customer.commonModel

data class ProductReviewModel(
    val `data`: ProductReviewData,
    val httpcode: Int,
    val status: String
)

data class ProductReviewData(
    val avg_rating: Int,
    val rate_range: RateRange,
    val review: List<Review>,
    val total_review: Int
)