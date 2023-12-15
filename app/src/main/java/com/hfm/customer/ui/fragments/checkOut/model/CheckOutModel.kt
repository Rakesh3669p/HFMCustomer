package com.hfm.customer.ui.fragments.checkOut.model

data class CheckOutModel(
    val `data`: CheckOutData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class CheckOutData(
    val address: List<Address>,
    val address_types: List<AddressType>,
    val faqs: List<Any>,
    val payment_methods: List<PaymentMethod>,
    val shipping_options: List<ShippingOption>
)

data class PaymentMethod(
    val desc: String,
    val id: Int,
    val title: String,
    val payment_type_id: Int,
    val is_online: Int
)

data class ShippingOption(
    val created_at: String,
    val id: Int,
    val is_active: Int,
    var is_selected: Boolean = false,
    val title: String,
    val updated_at: Any
)

data class AddressType(
    val addr_type_desc: String,
    val addr_type_id: Int,
    val addr_type_name: String
)

data class Address(
    val addr_id: Int,
    val address_1: String,
    val address_2: String,
    val address_type: String,
    val city_id: Int,
    val city_name: String,
    val country_id: Int,
    val country_name: String,
    val is_default_addr: Int,
    val latitude: String,
    val longitude: String,
    val pincode: String,
    val state_id: Int,
    val state_name: String
)