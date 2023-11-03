package com.hfm.customer.ui.fragments.store.model

import com.google.gson.annotations.SerializedName
import com.hfm.customer.commonModel.Review

data class StoreReviewsModel(
    val `data`: StoreReviewsData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class StoreReviewsData(
    val Reviews: List<Review>,
    val avg_reviews: Double,
    val rating_filter: RatingFilter,
    val total_reviews: Int
)
/*
data class Review(
    val comment: String,
    val customer_image: String,
    val customer_name: String,
    val image: Any,
    val rating: Int,
    val review_date: String,
    val review_time: String,
    val title: String
)*/
data class RatingFilter(
    @SerializedName("1 Star") val oneStar: Int,
    @SerializedName("2 Stars") val twoStar: Int,
    @SerializedName("3 Stars") val threeStar: Int,
    @SerializedName("4 Stars") val fourStar: Int,
    @SerializedName("5 Stars") val fiveStar: Int,
    val All: Int,
    val Media: Int
)