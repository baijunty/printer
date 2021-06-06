package com.baijunty.printer.lan

import android.util.Log
import com.baijunty.printer.Row
import com.baijunty.printer.bluetooth.BlueToothPrinter
import com.baijunty.printer.bluetooth.CommonBluetoothWriter
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.experimental.and

class JolimarkPrinterLanWriter(type: BlueToothPrinter.Type, charset: Charset, rows: List<Row>,
                               private val isEsc:Boolean) :
    CommonBluetoothWriter(type, charset, rows) {
    override fun printData(stream: OutputStream, inputStream: InputStream) {
        writer.reset()
        rows.forEach { row ->
            printerType.checkRowIllegal(row)
            writeRow(row)
        }
        val len = len
        stream.write(
            byteArrayOf(
                0xbc.toByte(),
                if (isEsc) 0x2 else 0x1,
                len.toByte() and 0xff.toByte(),
                (len shr 8).toByte() and 0xff.toByte(),
                (len shr 16).toByte() and 0xff.toByte(),
                (len shr 24).toByte() and 0xff.toByte()
            )
        )
        stream.write(writer.toByteArray())
        val bytes = inputStream.readBytes()
        isResponseSuccess(bytes)
    }

    private fun isResponseSuccess(bytes: ByteArray) {
        if (bytes.size > 6 && bytes[0] == 0xbc.toByte() && bytes[1] == 0x02.toByte()) {
            val len =
                bytes[2].toInt() or (bytes[3].toInt() shl 8) or (bytes[4].toInt() shl 16) or (bytes[5].toInt() shl 24)
            if (bytes.size >= len + 6) {
                val content = bytes.slice(6 until 6+len).toByteArray().toString(Charset.defaultCharset())
                val response = JSONObject(content)
                when (response.getInt("“status”")) {
                    0 -> {
                        Log.i("jolimark",content)
                    }
                    1 -> throw IllegalStateException("打印失败")
                    2 -> throw IllegalStateException("打印机任务解析失败")
                    else -> throw IllegalStateException("未知失败")
                }
                if (bytes.size>len+6&&response.getInt("progress")==1){
                    isResponseSuccess(bytes.slice(6+len until bytes.size).toByteArray())
                }
            }
        }
        throw IllegalStateException("返回信息不足")
    }

}