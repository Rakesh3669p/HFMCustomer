package com.hfm.customer.ui.fragments.address.model

data class CityCodeModel(
    val `data`: CityData,
    val httpcode: Int,
    val message: String,
    val status: String
)


data class CityData(
    val city: City
)
data class City(
    val city_name: String,
    val id: Int,
    val is_deleted: Int,
    val state_id: Int
)