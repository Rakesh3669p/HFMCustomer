package com.hfm.customer.commonModel

data class RatingReviewsModel(
    val `data`: RatingReviewsData,
    val httpcode: Int,
    val status: String,
    val message: String
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
    val date: String?,
    val image: List<String>,
    val product_variation: String,
    val rating: Int,
    val review_id: Int,
    val review_date: String,
    val review_time: String,
    val title: String,
    val video_link: String?,


)

data class RateRange(
    val FiveStars: Int,
    val FourStars: Int,
    val ThreeStars: Int,
    val TwoStars: Int,
    val OneStar: Int,
    val All: Int,
    val Media: Int
)