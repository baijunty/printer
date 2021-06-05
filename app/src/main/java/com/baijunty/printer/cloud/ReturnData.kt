package com.baijunty.printer.cloud


import com.google.gson.annotations.SerializedName

data class ReturnData(
    @SerializedName("access_token")
    val accessToken: String = "",
    @SerializedName("create_time")
    val createTime: String = "",
    @SerializedName("expires_in")
    val expiresIn: Int = 0
)