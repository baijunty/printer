package com.baijunty.printer.net

import android.content.Context
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection

class HttpPrinter(val printerWriter: PrinterWriter):PrintWorkModel {

    override val writer: PrinterWriter
        get() = printerWriter

    override fun print(context: Context): Observable<Boolean> {
        TODO("Not yet implemented")
    }

    override fun preview(context: Context): Observable<View> {
        return Observable.just(printerWriter)
            .observeOn(Schedulers.single())
            .map { it.preview().toString() }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val view= WebView(context)
                val settings = view.settings
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowContentAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.setSupportZoom(true)
                view.loadDataWithBaseURL(null,it,"text/HTML", "UTF-8", null)
                view
            }
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}