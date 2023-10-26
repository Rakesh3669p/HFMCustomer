package com.hfm.customer.ui.dashBoard.profile.model

data class ProfileModel(
    val `data`: ProfileData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class ProfileData(
    val profile: List<Profile>
)

data class Profile(
    val address1: String,
    val birthday: String?="",
    val business_address: BusinessAddress,
    val business_details: BusinessDetails,
    val city: String,
    val city_id: Any,
    val country: String,
    val country_code: Any,
    val country_id: Any,
    val credits: Int,
    val customer_type: String,
    val duration: String,
    val email: String,
    val first_name: String,
    val gender: Any,
    val invite_count: Int,
    val invite_save: Int,
    val joined_date: String,
    val last_name: String?="",
    val phone: String,
    val profile_image: String,
    val state: String,
    val state_id: Any,
    val user_id: Int,
    val username: String,
    val wallet: Any
)

data class BusinessDetails(
    val id:Int,
    val user_id:Int,
    val business_name:String,
    val established_on:String?=null,
    val business_type:String,
    val registration_no:String,
    val country_code:String,
    val contact_no:String,
    val is_branches:Int,
    val created_at:String,
    val updated_at:String,
    val category_name:String
)
data class BusinessAddress(
    val address_1: Any,
    val address_2: Any,
    val city_id: Any,
    val country_code: Any,
    val country_id: Any,
    val created_at: String,
    val created_by: Any,
    val email: Any,
    val house: Any,
    val id: Int,
    val is_active: Int,
    val is_default: Int,
    val is_deleted: Int,
    val latitude: String,
    val longitude: String,
    val name: Any,
    val neighborhood: Any,
    val org_id: Int,
    val phone: Any,
    val pincode: Any,
    val state_id: Any,
    val street: Any,
    val updated_at: String,
    val updated_by: Any,
    val user_id: Int,
    val usr_addr_typ_id: Int
)