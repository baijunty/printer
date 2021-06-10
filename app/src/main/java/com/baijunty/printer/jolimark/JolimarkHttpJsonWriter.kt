package com.baijunty.printer.jolimark

import com.baijunty.printer.PrinterWriter
import com.baijunty.printer.Row
import java.io.InputStream
import java.io.OutputStream

class JolimarkHttpJsonWriter(private val rows:List<Row>):PrinterWriter {
    override fun printData(stream: OutputStream, inputStream: InputStream) {
        TODO("Not yet implemented")
    }

    override fun preview(): CharSequence {
        TODO("Not yet implemented")
    }
}