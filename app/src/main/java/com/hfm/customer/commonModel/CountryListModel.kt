package com.hfm.customer.commonModel

data class CountryListModel(
    val `data`: CountryData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class CountryData(
    val country: List<Country>
)

data class Country(
    val country_name: String,
    val id: Int,
    val is_deleted: Int,
    val phonecode: Int,
    val sortname: String
)