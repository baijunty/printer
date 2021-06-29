package com.baijunty.printer.jolimark

import com.baijunty.printer.CommandCell
import com.baijunty.printer.PrinterWriter
import com.baijunty.printer.Row
import com.baijunty.printer.html.HtmlWriter
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalStateException
import java.net.URLEncoder

class JolimarkHttpWriter(private val rows:List<Row>):PrinterWriter {
    override fun printData(stream: OutputStream, inputStream: InputStream): Boolean {
        stream.write(rows.flatMap {
            it.columns.filterIsInstanceTo(mutableListOf(),CommandCell::class.java)
        }.fold(ByteArrayOutputStream()){s,c->
            s.write(c.getValue())
            s
        }.toByteArray())
        stream.write(URLEncoder.encode(preview().toString(),"utf-8").toByteArray())
        stream.flush()
        stream.close()
        val resp=JSONObject(inputStream.reader(Charsets.UTF_8).readText())
        inputStream.close()
        val success=resp.getInt("return_code")==0
        if (!success){
            throw IllegalStateException(resp.getString("return_msg"))
        }
        return true
    }

    override fun preview(): CharSequence {
        return HtmlWriter(rows).preview()
    }
}