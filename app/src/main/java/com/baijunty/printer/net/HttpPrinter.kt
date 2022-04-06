package com.baijunty.printer.net

import android.content.Context
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpPrinter(val printerWriter: PrinterWriter,private val url:String):PrintWorkModel {

    class LazyInputStream(conn:HttpURLConnection):InputStream(){
        val inputStream: InputStream by lazy {
            conn.inputStream
        }
        override fun read(): Int = inputStream.read()

        override fun close() {
            super.close()
            inputStream.close()
        }
    }

    override val writer: PrinterWriter
        get() = printerWriter

    override fun print(context: Context): Observable<Pair<Boolean, String>> {
        return Observable.just(url)
            .observeOn(Schedulers.computation())
            .map {
                val conn=URL(url).openConnection() as HttpURLConnection
                conn.requestMethod="POST"
                conn.doOutput=true
                conn.doInput=true
                conn.connect()
                val b=writer.printData(conn.outputStream,LazyInputStream(conn))
                conn.disconnect()
                b
            }
            .observeOn(AndroidSchedulers.mainThread())
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