package com.hfm.customer.ui.fragments.referral


data class ReferralModel(
    val httpcode: String,
    val message: String,
    val data: ReferralData,
)

data class ReferralData(
    val ref_code: String,
    val referral: List<Referral>,
)

data class Referral(
    val ref_code: String,
    val point_val: String,
    val ord_min_amount: Double,
    val max_invitations: String,
    val referral_rewards_expiry: String,
    val image: String,
    val referral_points: String,
)
