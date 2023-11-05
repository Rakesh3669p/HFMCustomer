package com.hfm.customer.commonModel

data class  SuccessModel (
    val httpcode: Int = 0,
    val success: String,
    val message: String,
    val status:String,
    val error:List<String>
)