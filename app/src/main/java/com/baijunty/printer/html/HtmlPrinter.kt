package com.baijunty.printer.html

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintJobInfo
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 使用[htmlWriter]生成内容
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class HtmlPrinter(private val htmlWriter: PrinterWriter): PrintWorkModel {

    override val writer: PrinterWriter
        get() = htmlWriter

    /**
     * 生成HTML文档并打印
     */
    override fun print(context: Context): Observable<Pair<Boolean, String>> {
        var job: PrintJob?=null
        return preview(context).concatMap { webView->
            Observable.create<Pair<Boolean, String>> {
                val view=webView as WebView
                val printService=context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val adapter= PrintDocumentAdapterWrapper(if (Build.VERSION.SDK_INT>=21) view.createPrintDocumentAdapter("default") else view.createPrintDocumentAdapter()) {
                    val status=when(job?.info?.state){
                        PrintJobInfo.STATE_BLOCKED -> {
                            "打印中"
                        }
                        PrintJobInfo.STATE_CANCELED -> {
                            "打印已取消"
                        }
                        PrintJobInfo.STATE_COMPLETED -> {
                            "打印完成"
                        }
                        PrintJobInfo.STATE_CREATED -> {
                            "打印任务已创建"
                        }
                        PrintJobInfo.STATE_FAILED -> {
                            "打印失败"
                        }
                        PrintJobInfo.STATE_QUEUED -> {
                            "打印已进入任务队列"
                        }
                        PrintJobInfo.STATE_STARTED -> {
                            "打印已开始"
                        }
                        else ->"未知失败"
                    }
                    it.onNext((job?.isStarted==true&&job?.isFailed==false&&job?.isCancelled==false) to (status))
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
        return Observable.just(htmlWriter)
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