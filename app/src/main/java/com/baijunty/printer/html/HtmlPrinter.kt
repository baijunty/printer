package com.baijunty.printer.html

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintJobInfo
import android.print.PrintManager
import android.webkit.WebView
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter

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
    override suspend fun print(context: Context): Pair<Boolean, String>{
        return preview(context).let {
            val view=it as WebView
            var status ="未知失败"
            val printService=context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            var job:PrintJob?=null
            val adapter= PrintDocumentAdapterWrapper(if (Build.VERSION.SDK_INT>=21) view.createPrintDocumentAdapter("default") else view.createPrintDocumentAdapter()) {
                status=when(job?.info?.state){
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
                    else -> status
                }
            }
            val jobName=System.currentTimeMillis().toString()
            job=printService?.print(jobName,adapter, PrintAttributes.Builder().build())
            (job?.isStarted==true && !job.isFailed && !job.isCancelled) to (status)
        }
    }

    override fun close() {
    }
}