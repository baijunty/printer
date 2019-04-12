package com.baijunty.printer.html

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob

/**
 * Created by bai on 2018-03-22.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class PrintDocumentAdapterWrapper(private val delegate :PrintDocumentAdapter, val listener: ()->Unit): PrintDocumentAdapter() {
    override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback?, extras: Bundle?) {
        delegate.onLayout(oldAttributes,newAttributes,cancellationSignal,callback,extras)
    }

    override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor?, cancellationSignal: CancellationSignal?, callback: WriteResultCallback?) {
        delegate.onWrite(pages,destination,cancellationSignal,callback)
    }

    override fun onFinish() {
        delegate.onFinish()
        listener()
    }

    override fun onStart() {
        delegate.onStart()
    }
}