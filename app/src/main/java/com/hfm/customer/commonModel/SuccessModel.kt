package com.hfm.customer.commonModel

data class  SuccessModel (
    val httpcode: Int = 0,
    val success: String,
    val message: String,
    val status:String,
    val data:ResponseData,
    val error:List<String>
)

data class ResponseData(
    val message: String,
)