package com.hfm.customer.ui.dashBoard.home.model

data class HomeBottomBannerModel(
    val `data`: BottomBannerData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BottomBannerData(
    val bottom_left_banner: List<BottomLeftBanner>,
    val bottom_right_banner: List<BottomRightBanner>,
    val user_data: List<Any>
)

data class BottomRightBanner(
    val button_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String
)

data class BottomLeftBanner(
    val button_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String
)