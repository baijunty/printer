package com.baijunty.printer.cloud


import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("return_code")
    val returnCode: Int = 0,
    @SerializedName("return_data")
    val returnData: T? = null,
    @SerializedName("return_msg")
    val returnMsg: String = ""
)