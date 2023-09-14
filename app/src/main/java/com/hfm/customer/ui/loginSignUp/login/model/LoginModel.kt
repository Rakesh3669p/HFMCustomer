package com.hfm.customer.ui.loginSignUp.login.model

data class LoginModel(
    val `data`: LoginData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class LoginData(
    val access_token: String,
    val user_details: UserDetails
)

data class UserDetails(
    val email: String,
    val fname: String,
    val lname: String,
    val phone: String,
    val user_id: Int
)