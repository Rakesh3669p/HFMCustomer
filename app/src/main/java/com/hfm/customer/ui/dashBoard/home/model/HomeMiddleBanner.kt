package com.hfm.customer.ui.dashBoard.home.model

data class HomeMiddleBanner(
    val `data`: MiddleBannerData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class MiddleBannerData(
    val app_link_banner: List<AppLinkBanner>,
    val center_left_banner: List<CenterLeftBanner>,
    val center_right_banner: List<CenterRightBanner>,
    val user_data: List<Any>
)

data class AppLinkBanner(
    val appstore_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val playstore_link: String,
    val title: String
)

data class CenterLeftBanner(
    val button_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String,
    val category: String,
    val link_type: String,
    val product_id: String,
    val subcategory_id: String
)

data class CenterRightBanner(
    val button_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String
)