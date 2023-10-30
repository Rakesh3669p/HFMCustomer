package com.hfm.customer.ui.fragments.orderTracking

data class OrderTrackingModel(
    val `data`: TrackingData,
    val httpcode: String,
    val message: String,
    val status: String
)

data class TrackingData(
    val delivery_proof: String,
    val events: List<Event>,
    val shipping_method: String
)

data class Event(
    val detDate: String,
    val detTime: String,
    val CP_Code: String,
    val status: String,
    val location: String,
    val Recipient: String,
    val DisposeCode: String,

    val date: String,
    val description: String,
    val serviceArea: List<ServiceArea>,
    val signedBy: String,
    val time: String,
    val typeCode: String
)

data class ServiceArea(
    val code: String,
    val description: String
)