package com.hfm.customer.ui.fragments.address.model

data class CountryCodeModel(
    val `data`: CountryData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class CountryData(
    val country: Country
)

data class Country(
    val country_name: String,
    val id: Int,
    val is_deleted: Int,
    val phonecode: Int,
    val sortname: String
)