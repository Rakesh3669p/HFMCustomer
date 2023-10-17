package com.hfm.customer.ui.fragments.wallet.model


data class WalletModel(
    val `data`: WalletData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class WalletData(
    val tot_credit: String,
    val tot_debit: String,
    val total_balance: Double,
    val wallet: List<Wallet>
)


data class Wallet(
    val created_at: String,
    val credit: String,
    val credit_value: Boolean,
    val debit: String,
    val id: Int,
    val source: String,
    val source_ids: String
)