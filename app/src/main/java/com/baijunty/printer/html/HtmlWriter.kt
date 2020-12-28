package com.baijunty.printer.html

import android.graphics.BitmapFactory
import com.baijunty.printer.*

class HtmlWriter(private val rows: List<Row>, private val border: Int = 1) : PrinterWriter {
    /**
     * 正是打印字节，html页面使用预览页面即可
     * @return 空
     */
    override fun print(): ByteArray = byteArrayOf()

    /**
     * 正式正成HTML页面
     * @return 页面内容字符
     */
    private fun buildHtmlContent(): String {
        val chunk=mutableListOf<MutableList<Row>>()
        rows.fold(-1){ acc, row ->
            val group=if (acc!=row.columns.size){
                val g=mutableListOf<Row>()
                chunk.add(g)
                g
            } else chunk.last()
            group.add(row)
            row.columns.size
        }
        return html(border) {
            chunk.forEach{
                tag("table"){
                    it.forEach {
                        this.writeRow(it)
                    }
                    ""
                }
            }
        }.toString()
    }

    private fun Tag.writeRow(row: Row){
        tag("tr"){
            row.columns.forEach {
                tag("td") {
                    when (it) {
                        is TextCell -> {
                            writeStyle(it)
                            writeContentByAlign(it)
                        }
                        is ImageCell -> {
                            tag("div") {
                                when (it.type) {
                                    is BarCode -> {
                                        writeBarCode(
                                            it.content,
                                            it.type.type,
                                            it.width,
                                            it.height
                                        )
                                    }
                                    QRCode -> {
                                        writeQrCode(it.content, it.width, it.height)
                                    }
                                    Image -> {
                                        val bytes = it.getValue()
                                        val bitmap = BitmapFactory.decodeByteArray(
                                            bytes,
                                            0,
                                            bytes.size
                                        )
                                        writeBitmap(bitmap, it.width, it.height)
                                    }
                                }
                                ""
                            }
                            ""
                        }
                        is CommandCell -> ""
                    }
                }
            }
            ""
        }
    }

    /**
     * 设置文字效果
     * @param cell 要设置的单元格
     */
    private fun Tag.writeStyle(cell: TextCell) {
        //双倍大小
        if (cell.style.double) {
            writeHeighten()
        }
        //加粗
        if (cell.style.bold) {
            writeBold()
        }
        //下划线
        if (cell.style.underLine) {
            writeUnderLine()
        }
    }

    /**
     * 写入单元格内容和格式
     * @param cell 要写入的单元格
     * 返回空字符
     */
    private fun Tag.writeContentByAlign(cell: TextCell): String {
        return when (cell.align) {
            Align.CENTER -> {
                cls("center")
                tag("div") {
                    cell.getValue()
                }
                ""
            }
            Align.RIGHT -> {
                cls("right")
                tag("div") {
                    cell.getValue()
                }
                ""
            }
            Align.LEFT -> {
                cls("left")
                tag("div") {
                    cell.getValue()
                }
                ""
            }
            Align.FILL -> {
                tag("hr") { "" }
                ""
            }
        }
    }

    /**
     *
     *@return 返回打印预览用字符串
     */
    override fun preview(): CharSequence = buildHtmlContent()

    override fun toString(): String = preview().toString()
}