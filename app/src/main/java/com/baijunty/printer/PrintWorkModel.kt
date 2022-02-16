package com.baijunty.printer

import android.content.Context
import android.view.View
import android.webkit.WebView
import java.io.Closeable

/**
 * Created by bai on 2018-02-08.
 * 打印任务
 * @property writer 用于生成打印内容
 */
interface PrintWorkModel : Closeable {
    suspend fun print(context: Context) : Pair<Boolean,String>
    suspend fun preview(context : Context): View {
        val view= WebView(context)
        val settings = view.settings
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.setSupportZoom(true)
        view.loadDataWithBaseURL(null,writer.preview().toString(),"text/HTML", "UTF-8", null)
        return view
    }
    val writer:PrinterWriter
}