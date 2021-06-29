package com.baijunty.printer.jolimark

import com.baijunty.printer.BarCodeType
import com.baijunty.printer.Row
import com.baijunty.printer.bluetooth.BlueToothPrinter
import com.baijunty.printer.bluetooth.CommonBluetoothWriter
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.experimental.and

/**
 * 映美云打印机使用蓝牙打印
 */
class JolimarkBluetoothPrinterWriter(type: BlueToothPrinter.Type, charset: Charset, rows:List<Row>,private val isEsc:Boolean)
    : CommonBluetoothWriter(type, charset, rows) {
    override fun printData(stream: OutputStream, inputStream: InputStream) :Boolean{
        writer.reset()
        rows.forEach { row ->
            printerType.checkRowIllegal(row)
            writeRow(row)
        }
        val len=len
        stream.write(byteArrayOf(0x1,if (isEsc) 0x50 else 0x52,(len shr 16).toByte() and 0xff.toByte(),(len shr 8).toByte() and 0xff.toByte(),len.toByte() and 0xff.toByte()))
        stream.write(writer.toByteArray())
        return true
    }
}