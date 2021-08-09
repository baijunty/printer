package com.baijunty.printer.jolimark

import android.util.Log
import com.baijunty.printer.EncryptUtil.md5
import com.baijunty.printer.Row
import com.baijunty.printer.bluetooth.BlueToothPrinter
import com.baijunty.printer.bluetooth.CommonBluetoothWriter
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

class JolimarkPrinterLanWriter(type: BlueToothPrinter.Type, charset: Charset, rows: List<Row>) :
    CommonBluetoothWriter(type, charset, rows) {
    override fun printData(stream: OutputStream, inputStream: InputStream): Pair<Boolean, String> {
        val jsonObject = JSONObject().apply {
            put("tp", "1002")
            put("paper_type",3)
            put("paper_width",208)
            put("taskid", System.currentTimeMillis().toString())
            put("pdata", preview().toString())
        }
        println(jsonObject.toString())
        val content =jsonObject.toString().toByteArray(Charsets.UTF_8)
        val len = content.size
        val command = byteArrayOf(
            0xbc.toByte(),
            0x1,
            (len and 0xff).toByte(),
            ((len shr 8) and 0xff).toByte(),
            ((len shr 16) and 0xff).toByte(),
            ((len shr 24) and 0xff).toByte()
        )
        println(command.contentToString())
        val bytes = stream.use { outputStream ->
            outputStream.write(command)
            outputStream.write(content)
            outputStream.flush()
            inputStream.use {
                it.readBytes()
            }
        }
        println(bytes.toString(Charset.defaultCharset()))
        return isResponseSuccess(bytes)
    }

    private fun isResponseSuccess(bytes: ByteArray):Pair<Boolean, String> {
        if (bytes.size > 6 && bytes[0] == 0xbc.toByte()) {
            val len =
                bytes[2].toInt() or (bytes[3].toInt() shl 8) or (bytes[4].toInt() shl 16) or (bytes[5].toInt() shl 24)
            if (bytes.size >= len + 6) {
                val content =
                    bytes.slice(6 until 6 + len).toByteArray().toString(Charset.defaultCharset())
                val response = JSONObject(content)
                when (response.getInt("status")) {
                    0 -> {
                        Log.i("jolimark", content)
                    }
                    else -> throw IllegalStateException(if (response.getInt("progress")==1) "打印任务解析失败" else "打印失败")
                }
                if (bytes.size > len + 6 && response.getInt("progress") == 1) {
                    return isResponseSuccess(bytes.slice(6 + len until bytes.size).toByteArray())
                }
                return true to content
            }
        }
        return false to "打印机应答错误,${bytes.toString(Charset.defaultCharset())}"
    }

}