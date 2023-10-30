package com.hfm.customer.commonModel

data class HomeMainCategoriesModel(
    val data: BannerData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BannerData(
    val cat_subcat: List<CatSubcat>,
    val custom_links: List<CustomLink>,
    val app_top_banner: List<MainBanner>,
    val app_bottom_banner: List<MainBanner>,
    val search_placeholder_text: String,
    val top_bar_banner: List<TopBarBanner>,
    val user_data: List<Any>,
    val promotion_popup:PromotionPopup
)

data class PromotionPopup(
    val promotion_image: String,
    val promotion_link: String,
    val category: String,
    val sub_category: String,
)

data class CatSubcat(
    val category_id: Int,
    val category_name: String,
    val image: String,
    val subcategory: List<Subcategory>
)

data class CustomLink(
    val created_at: String,
    val created_by: Int,
    val custom_link: String,
    val id: Int,
    val is_active: Int,
    val is_deleted: Int,
    val link_title: String,
    val link_type: String,
    val updated_at: String,
    val updated_by: Int
)

data class MainBanner(
    val alt_text: Any,
    val button_label: String,
    val button_link: String,
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String
)

data class Subcategory(
    val id: Int,
    val subcategory_children: List<SubcategoryChildren>,
    val subcategory_image: String,
    val subcategory_name: String
)

data class SubcategoryChildren(
    val id: Int,
    val subcategory_image: String,
    val subcategory_name: String
)

data class TopBarBanner(
    val description: String,
    val id: Int,
    val identifier: String,
    val media: String,
    val media_type: String,
    val title: String
)