package com.baijunty.printer.bluetooth

import com.baijunty.printer.Row
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.experimental.and

/**
 * 映美云打印机使用蓝牙打印
 */
class JolimarkPrinterWriter(type: BlueToothPrinter.Type, charset: Charset, rows:List<Row>):CommonBluetoothWriter(type, charset, rows) {
    override fun printData(stream: OutputStream, inputStream: InputStream) {
        writer.reset()
        rows.forEach { row ->
            printerType.checkRowIllegal(row)
            writeRow(row)
        }
        val len=len
        stream.write(byteArrayOf(0x1,0x50,(len shr 16).toByte() and 0xff.toByte(),(len shr 8).toByte() and 0xff.toByte(),len.toByte() and 0xff.toByte()))
        stream.write(writer.toByteArray())
    }
}