package com.hfm.customer.ui.fragments.blogs.model

data class BlogsModel(
    val `data`: BlogsData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BlogsData(
    val categories: List<Category>,
    val list: List<Blogs>
)

data class Blogs(
    val author: String,
    val blog_id: Int,
    val blog_title: String,
    val content: String,
    val image: String,
    val video_link: String
)

data class Category(
    val id: Int,
    val name: String
)