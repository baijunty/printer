package com.baijunty.printer.jolimark

import android.util.Log
import com.baijunty.printer.CommandCell
import com.baijunty.printer.PrinterWriter
import com.baijunty.printer.Row
import com.baijunty.printer.bluetooth.BlueToothPrinter
import com.baijunty.printer.html.HtmlWriter
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.net.URLEncoder

class JolimarkHttpWriter(
    private val rows: List<Row>,
    private val isEsc: Boolean,
    private val printer: BlueToothPrinter.Type
) : PrinterWriter {

    private fun writeEsc(stream: OutputStream) {
        val params = rows.takeWhile { row -> row.columns.any { it is CommandCell } }
        stream.write(params.flatMap {
            it.columns.filterIsInstanceTo(mutableListOf(), CommandCell::class.java)
        }.fold(ByteArrayOutputStream()) { s, c ->
            s.write(c.getValue())
            s
        }.toByteArray())
        val content = rows.subList(params.size, rows.size)
        JolimarkBluetoothPrinterWriter(printer, Charsets.UTF_8, content, true).printData(
            stream, ByteArrayInputStream(
                ByteArray(0)
            )
        )
        stream.flush()
        stream.close()
    }

    private fun writeHttp(stream: OutputStream) {
        stream.write(rows.flatMap {
            it.columns.filterIsInstanceTo(mutableListOf(), CommandCell::class.java)
        }.fold(ByteArrayOutputStream()) { s, c ->
            s.write(c.getValue())
            s
        }.toByteArray())
        stream.write(URLEncoder.encode(preview().toString(), "utf-8").toByteArray())
        stream.flush()
        stream.close()
    }

    override fun printData(stream: OutputStream, inputStream: InputStream): Pair<Boolean, String> {
        if (isEsc) writeEsc(stream) else writeHttp(stream)
        val resp = JSONObject(inputStream.reader(Charsets.UTF_8).readText())
        inputStream.close()
        Log.e("jolimark", resp.toString())
        val success = resp.getInt("return_code") == 0
        var msg=if (success) resp.getString("return_data") else resp.getString("return_msg")
        if (resp.has("printer_state")){
            val array=resp.getJSONArray("printer_state")
            val status=(0 until array.length()).fold(StringBuilder()){sb,i->
                if (sb.isNotEmpty()){
                    sb.append(',')
                }
                sb.append(array.getJSONObject(i).getString("status_msg"))
            }
            msg="$msg,打印机状态[$status]"
        }
        return success to msg
    }

    override fun preview(): CharSequence {
        return HtmlWriter(rows,useDash = true).preview()
    }
}