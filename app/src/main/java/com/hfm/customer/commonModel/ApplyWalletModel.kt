package com.hfm.customer.commonModel

data class ApplyWalletModel(
    val `data`: WalletData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class WalletData(
    val wallet: Wallet
)

data class Wallet(
    val amount: Double,
    val points: Double,
    val points_val: Int,
    val user_id: Int
)