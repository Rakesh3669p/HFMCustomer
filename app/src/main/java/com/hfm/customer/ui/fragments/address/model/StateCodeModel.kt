package com.hfm.customer.ui.fragments.address.model

data class StateCodeModel(
    val `data`: StateData,
    val httpcode: Int,
    val message: String,
    val status: String
)

data class StateData(
    val state: State
)

data class State(
    val country_id: Int,
    val division: Any,
    val id: Int,
    val is_deleted: Int,
    val state_code: String,
    val state_name: String
)