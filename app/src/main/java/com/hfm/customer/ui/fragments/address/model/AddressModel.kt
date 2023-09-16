package com.hfm.customer.ui.fragments.address.model

data class AddressModel(
    val `data`: AddressData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class AddressData(
    val address_list: List<Address>
)

data class Address(
    val address1: String?="",
    val address2: String?="",
    val address_type: String,
    val address_type_id: Int,
    val city: String,
    val city_id: String?="",
    val country: String,
    val country_code: String?="",
    val country_id: String?="",
    val house: String?="",
    val id: Int,
    val is_default: Int,
    val latitude: String,
    val longitude: String,
    val name: String?="",
    val neighborhood: String?="",
    val phone: String?="",
    val pincode: String?="",
    val state: String,
    val state_id: String?="",
    val street: String?=""
)