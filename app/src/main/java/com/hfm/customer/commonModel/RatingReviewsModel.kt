package com.hfm.customer.commonModel

data class RatingReviewsModel(
    val `data`: RatingReviewsData,
    val httpcode: Int,
    val status: String
)

data class RatingReviewsData(
    val avg_rating: Int,
    val rate_range: RateRange,
    val review: List<Review>,
    val total_review: Int
)

data class Review(
    val comment: String,
    val customer_image: String,
    val customer_name: String,
    val date: String,
    val image: String,
    val product_variation: String,
    val rating: Int,
    val review_id: Int,
    val title: String,
    val video_link: String
)

data class RateRange(
    val oneStar: Int,
    val twoStars: Int,
    val threeStars: Int,
    val fourStars: Int,
    val fiveStars: Int,
    val All: Int,
    val Media: Int
)