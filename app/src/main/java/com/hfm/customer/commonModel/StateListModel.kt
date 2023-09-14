package com.hfm.customer.commonModel

data class StateListModel(
    val data: StateData,
    val httpcode: Int,
    val message: String,
    val status: String
)


data class StateData(
    val state: List<State>
)

data class State(
    val country_id: Int,
    val id: Int,
    val is_deleted: Int,
    val state_code: String,
    val state_name: String
)