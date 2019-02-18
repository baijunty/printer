package com.uplus.printer.html

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentActivity
import android.view.View
import android.webkit.WebView
import com.uplus.printer.PrintWorkModel
import com.uplus.printer.PrinterListener
import com.uplus.printer.PrinterWriter
import print.PrintDocumentAdapterWrapper
/**
 * 使用[htmlWriter]生成内容，[activity]生成预览View
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class HtmlPrinter(private val activity: FragmentActivity, private var htmlWriter: PrinterWriter): PrintWorkModel {
    override var writer: PrinterWriter
        get() = htmlWriter
        set(value) {
            htmlWriter=value
        }
    var job:PrintJob?=null
    /**
     * 生成HTML文档并打印
     * [listener]回调打印结果
     */
    override fun print(listener: PrinterListener) {
        val view= preview() as WebView
        val printService=activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter= PrintDocumentAdapterWrapper(if (Build.VERSION.SDK_INT>=21) view.createPrintDocumentAdapter("default") else view.createPrintDocumentAdapter()) {
            listener.onFinish(job?.isStarted==true&&job?.isFailed==false&&job?.isCancelled==false,null)
        }
        val jobName=System.currentTimeMillis().toString()
        job=printService.print(jobName,adapter, PrintAttributes.Builder().build())
    }
    /**
     * 生成HTML预览文档View
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun preview(): View {
        val view=WebView(activity)
        val settings = view.settings;
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.javaScriptEnabled = true;
        settings.setSupportZoom(true)
        view.loadDataWithBaseURL(null,writer.preview().toString(),"text/HTML", "UTF-8", null)
        return view
    }


    /**
     * [activity]结束取消任务
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun close() {
        job?.cancel()
    }
}