package com.baijunty.printer.net

import android.content.Context
import com.baijunty.printer.PrintWorkModel
import com.baijunty.printer.PrinterWriter
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

    override suspend fun print(context: Context): Pair<Boolean, String> {
        var conn:HttpURLConnection?=null
        try {
            conn=URL(url).openConnection() as HttpURLConnection
            conn.requestMethod="POST"
            conn.doOutput=true
            conn.doInput=true
            conn.connect()
            val b=writer.printData(conn.outputStream,LazyInputStream(conn))
            conn.disconnect()
            return b
        } finally {
            conn?.disconnect()
        }
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}