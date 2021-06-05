package com.baijunty.printer.cloud

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface JolimarkCloudPrintApi {
}

interface InitCloudPrinter{
    @GET("GetAccessToken")
    fun getAccessToken():Observable<BaseResponse<ReturnData>>
}