package com.hfm.customer.ui.fragments.commonPage.model

data class PageModel(
    val `data`: PageData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class PageData(
    val pages: Pages
)

data class Pages(
    val content: String,
    val identifier: String,
    val page_id: Int,
    val title: String
)