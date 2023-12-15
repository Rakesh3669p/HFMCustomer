package com.hfm.customer.ui.fragments.products.productDetails.model

data class UOMModel(
    val httpcode: Int = 0,
    val message: String,
    val status:String,
    val data:UOMData,
)

data class UOMData(
    val UOM:List<UOM>
)

data class UOM(
    val id:Int,
    val uom:String,
)


