package com.hfm.customer.ui.dashBoard.home.model

data class TrendingNowModel(
    val `data`: TrendingNowData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class TrendingNowData(
    val events: List<Events>
)

data class Events(
    val image: String,
    val link: String,
    val category: String,
    val sub_category: String
)