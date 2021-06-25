package com.baijunty.printer

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.bluetooth.DelegateInputStream
import com.baijunty.printer.bluetooth.DelegateOutputStream
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.io.OutputStream
import kotlin.properties.Delegates

abstract class AbstractSocketPrinter(var printerWriter: PrinterWriter): PrintWorkModel {

    var printTime: Int = 1
    var address: String by Delegates.observable("") { _, o, n ->
        if (o != n) {
            kotlin.runCatching {
                close()
            }
        }
    }

    abstract fun createSocket()

    abstract fun getOutputStream():OutputStream

    abstract fun getInputStream():InputStream

    override val writer: PrinterWriter
        get() = printerWriter

    /**
     ** 正式打印
     ** @param context
     * @return
     */
    override fun print(context: Context): Observable<Boolean> {
        return Observable.just(printerWriter)
            .observeOn(Schedulers.single())
            .map {
                createSocket()
                var s = false
                for (i in 0 until printTime) {
                    s = tryWrite()
                    if (!s) {
                        close()
                        createSocket()
                        s = tryWrite()
                    }
                }
                s
            }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * [context]生成预览数据View
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun preview(context: Context): Observable<View> {
        return Observable.just(printerWriter)
            .map { it.preview().toString() }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val view = WebView(context)
                val settings = view.settings
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowContentAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
                view.loadDataWithBaseURL(null, it, "text/HTML", "UTF-8", null)
                view  as View
            }.subscribeOn(Schedulers.single())
    }


    protected fun tryWrite(): Boolean {
        return runCatching {
            printerWriter.printData(getOutputStream(),getInputStream())
            true
        }.onFailure {
            it.printStackTrace()
        }.getOrDefault(false)
    }
}