package com.baijunty.printer.html

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 使用[htmlWriter]生成内容
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class HtmlPrinter(private var htmlWriter: PrinterWriter): PrintWorkModel {
    override var writer: PrinterWriter
        get() = htmlWriter
        set(value) {
            htmlWriter=value
        }
    /**
     * 生成HTML文档并打印
     * [listener]回调打印结果
     */
    override fun print(context: Context): Observable<Boolean> {
        var job: PrintJob?=null
        return preview(context).concatMap { webView->
            Observable.create<Boolean> {
                val view=webView as WebView
                val printService=context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val adapter= PrintDocumentAdapterWrapper(if (Build.VERSION.SDK_INT>=21) view.createPrintDocumentAdapter("default") else view.createPrintDocumentAdapter()) {
                    it.onNext(job?.isStarted==true&&job?.isFailed==false&&job?.isCancelled==false)
                    it.onComplete()
                }
                val jobName=System.currentTimeMillis().toString()
                job=printService?.print(jobName,adapter, PrintAttributes.Builder().build())
            }
        }.doOnDispose {
            job?.cancel()
        }
    }

    /**
     * 生成HTML预览文档View
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun preview(context: Context): Observable<View> {
        return Observable.just(writer)
            .map { it.preview().toString() }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val view=WebView(context)
                val settings = view.settings
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowContentAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
                view.loadDataWithBaseURL(null,it,"text/HTML", "UTF-8", null)
                view as View
            }.subscribeOn(Schedulers.single())
    }

    override fun close() {
    }
}