package com.baijunty.printer

import android.content.Context
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
    override suspend fun print(context: Context): Pair<Boolean, String> {
        createSocket()
        var s = false to ""
        for (i in 0 until printTime) {
            s = tryWrite()
            if (!s.first) {
                close()
                createSocket()
                s = tryWrite()
            }
        }
        return s
    }



    protected fun tryWrite(): Pair<Boolean, String> {
        return runCatching {
            printerWriter.printData(getOutputStream(),getInputStream())
        }.getOrThrow()
    }
}