package com.hfm.customer.ui.fragments.blogs.model

data class BlogDetailsModel(
    val `data`: BlogDetailsData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class BlogDetailsData(
    val post: Post
)

data class Post(
    val author: String,
    val blog_id: Int,
    val blog_title: String,
    val content: String,
    val image: String,
    val video_link: Any
)