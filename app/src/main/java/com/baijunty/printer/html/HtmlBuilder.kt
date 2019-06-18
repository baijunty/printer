package com.baijunty.printer.html

import android.graphics.Bitmap
import android.util.Base64
import com.baijunty.printer.FormatWriter
import com.baijunty.printer.toBarCodeBitmap
import com.baijunty.printer.toQrCodeBitmap
import java.io.ByteArrayOutputStream

/**
 * HTML文档定义生成，[name]Tag名称,[content]内容
 * @property properties 属性列表
 * @property contents 子内容
 */
open class Tag(val name: String, var content: HtmlTagWriter? = null) : HtmlTagWriter {
    val properties = mutableListOf<Property>()
    private val contents = mutableListOf<HtmlTagWriter>()
    override fun write(sb: StringBuilder) {
        sb.append('<').append(name)
        properties.fold(sb) { s, it ->
            it.write(s)
            s
        }
        sb.append('>')
        contents.forEach {
            it.write(sb)
        }
        content?.write(sb)
        sb.append("</").append(name).append('>')
    }

    fun addChild(writer: HtmlTagWriter): Tag {
        contents.add(writer)
        return this
    }

    fun addProperty(writer: Property): Tag {
        properties.add(writer)
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder()
        write(sb)
        return sb.toString()
    }

    override fun writeBold() {
        cls("bold")
    }

    override fun writeLf() {
    }

    override fun writeHeighten() {
        cls("double")
    }

    override fun writeBitmap(bitmap:Bitmap) {
        tag("img"){
            prop("src"){
                val out= ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG,100,out)
                "data:image/png;base64,"+ Base64.encodeToString(out.toByteArray(),Base64.DEFAULT)
            }
            ""
        }
    }

    override fun writeQrCode(v: String, width: Int, height: Int) {
        val w=if (width<=0) 400 else width
        val h=if (height<=0) w else height
        writeBitmap(toQrCodeBitmap(v,w,h))
    }

    override fun writeBarCode(v: String, type: Int) {
        writeBitmap(toBarCodeBitmap(v,type==72))
    }

    override fun writeUnderLine() {
        cls("underline")
    }

    override fun cutPaper() {
    }

    override fun clean() {
    }

    override fun writeCenter() {
        cls("center")
    }
}

class CssValue<V>(name: String) : Tag(name) {
    private var value: V? = null
    override fun write(sb: StringBuilder) {
        sb.append(name).append(':').append(value ?: "").append(';')
    }

    fun initValue(v: V) {
        value = v
    }
}

open class Property(name: String) : Tag(name) {
    override fun write(sb: StringBuilder) {
        sb.append(' ').append(name).append("=\"")
        content?.write(sb)
        sb.append('"')
    }
}

class ConstWrite(val value: String) : Tag("") {
    override fun write(sb: StringBuilder) {
        sb.append(value)
    }
}

class ClassProperty : Property("class") {
    private val values = mutableListOf<ConstWrite>()
    override fun write(sb: StringBuilder) {
        sb.append(' ').append(name).append("=\"")
        values.forEach {
            it.write(sb)
            sb.append(' ')
        }
        sb.append('"')
    }

    fun addClass(name: String) {
        values.add(ConstWrite(name))
    }

}

class CssProperty(name: String) : Property(name) {
    val values = mutableListOf<CssValue<*>>()
    override fun write(sb: StringBuilder) {
        sb.append(' ').append(name).append("=\"")
        values.forEach {
            it.write(sb)
        }
        sb.append('"')
    }
}

class Table : Tag("table")
class HtmlRow : Tag("tr")
class HtmlCol(name: String) : Tag(name)

fun Table.tr(init: HtmlRow.() -> Unit={}): HtmlRow {
    val row = HtmlRow()
    row.init()
    addChild(row)
    return row
}


fun Tag.style(init: CssProperty.() -> Unit) {
    val style = CssProperty("style")
    style.init()
    addProperty(style)
}

fun Tag.cls(vararg name: String) {
    val style: ClassProperty = properties.find { it.name=="class" } as? ClassProperty
            ?: ClassProperty()
    name.forEach {
        style.addClass(it)
    }
    addProperty(style)
}

fun HtmlRow.th(init: HtmlCol.() -> String) {
    val th = HtmlCol("th")
    th.content = ConstWrite(th.init())
    addChild(th)
}

fun HtmlRow.td(init: HtmlCol.() -> String) {
    val td = HtmlCol("td")
    td.content = ConstWrite(td.init())
    addChild(td)
}

fun HtmlCol.rows(int: Int) {
    val p = Property("rowspan")
    p.content = ConstWrite(int.toString())
    addProperty(p)
}

fun HtmlCol.cols(int: Int) {
    val p = Property("colspan")
    p.content = ConstWrite(int.toString())
    addProperty(p)
}

fun Tag.prop(name: String, init: Property.() -> String) {
    val p = Property(name)
    p.content = ConstWrite(p.init())
    addProperty(p)
}

fun Tag.tag(name: String, init: Tag.() -> String) {
    val p = Tag(name)
    p.content = ConstWrite(p.init())
    addChild(p)
}

fun <V> CssProperty.string(name: String, init: CssValue<*>.() -> V) {
    val value = CssValue<V>(name)
    value.initValue(value.init())
    values.add(value)
}

fun CssProperty.string(name: String, value: String) {
    val cssValue = CssValue<String>(name)
    cssValue.initValue(value)
    values.add(cssValue)
}
fun Tag.table(init: Table.() -> Unit) {
    val html = Table()
    html.init()
    addChild(html)
}

fun html(border:Int=1,init: Tag.() -> Unit): Tag {
    val html = Tag("html")
    val head= Tag("head")
    head.addChild(
        ConstWrite("""
        <style>
      .flex-container {
  display: flex;
  justify-content: center;
  align-items: stretch;
}

.flex-container > div {
border:1px solid black;
  color: blank;
  display: flex;
  justify-content: center;
  flex-flow: column wrap;
}
         .center{
           text-align: center;
         }
         .right{
           text-align: right;
         }
         .left{
           text-align: left;
         }
         .underline {
            text-decoration:underline
         }
         .double{
           font-size:200%;
         }
         .half{
           width:50%
         }
         .bold{
            font-weight:bold;
         }
         </style>
            """
    )
    )
    html.addChild(head)
    val body = Tag("body")
    body.init()
    html.addChild(body)
    return html
}

interface HtmlTagWriter: FormatWriter {
    fun write(sb: StringBuilder)
}