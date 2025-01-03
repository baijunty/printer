package com.baijunty.printer.bluetooth

import android.graphics.BitmapFactory
import android.graphics.Rect
import com.baijunty.printer.*
import com.baijunty.printer.html.HtmlWriter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.math.max

/**
 * 按[printerType]类型,[charset]编码,[rows]行定义生成打印格式内容
 * @property top 定义当前光标纵向位置
 * @property left 定义当前光标横向位置
 * @property writer 缓存写入内容
 */
abstract class BlueToothWriter(
    printerType: BlueToothPrinter.Type,
    charset: Charset,
    rows: List<Row>
) :
    ContentWriter(printerType, charset, rows) {
    var top = 0
    var left = 0
    protected val writer = ByteArrayOutputStream()
    /**
     * @param row 行定义
     * @return 根据[printerType]定义行每一列的列宽区域
     */
    protected fun getRowRect(row: Row): List<Rect> {
        if (row.anchor>=0&&rows[row.anchor].columns.size>=row.columns.size){
            return getRowRect(rows[row.anchor])
        }
        val len = printerType.len
        val size = row.columns.size
        val leftSpace = len - (row.gap) * (size - 1)
        val totalWeight = row.columns.fold(0) { acc, column -> acc + column.weight }
        val avgColLen = leftSpace.toDouble() / totalWeight.toDouble()
        val columnsRect = ArrayList<Rect>(size)
        var left = 0
        if (avgColLen>2){
            for ((index, column) in row.columns.withIndex()) {
                val rect = Rect()
                rect.left = left
                rect.right =
                    if (index == size - 1) len else (left + avgColLen * (column.weight)).toInt()
                left = rect.right + row.gap
                columnsRect.add(rect)
            }
        } else {
            for ((index, column) in row.columns.withIndex()) {
                val rect = Rect()
                rect.left = left
                val length=if (column is TextCell) column.getValue().fold(0){acc, c ->
                    acc+c.len()
                } else avgColLen.toInt()
                rect.right =
                    if (index == size - 1) len else (left + length * (column.weight))
                left = if (rect.right + row.gap>len) rect.right + row.gap-len else rect.right + row.gap
                columnsRect.add(rect)
            }
        }
        return columnsRect
    }

    /**
     * 已写入字节数
     */
    val len: Int
        get() = writer.size()
    /**
     * 横向移动光标[num]位
     */
    protected fun moveLeft(num: Int) {
        left += num
        if (left >= printerType.len) {
            left %= printerType.len
        }
    }

    /**
     * 写入[row]行数据
     * @return 返回写入的行数
     */
    protected open fun writeRow(row: Row) {
        if (row.rangeLimit) {
            writeLimitRow(row)
        } else {
            writeUnLimitRow(row)
        }
        val needClean =
            row.columns.any { column -> column is TextCell && (column.style.bold || column.style.double) }
        if (needClean) {
            clean()
        }
    }

    /**
     * 写入列宽度受限[row]行数据
     */
    protected open fun writeLimitRow(row: Row): Int {
        val rs = getRowRect(row)
        var height = 0
        if (row.rangeLimit) {
            for ((index, column) in row.columns.withIndex()) {
                if (column is TextCell) {
                    height = max(height, getHeight(column, rs[index].width()))
                }
            }
        }
        val columnsPos = IntArray(row.columns.size)
        for (i in 0 until height) {
            for ((index, column) in row.columns.withIndex()) {
                if (columnsPos[index] >= 0) {
                    val rect = rs[index]
                    when (column) {
                        is TextCell -> {
                            val width = rect.width()
                            val v = column.getValue()
                            val start = columnsPos[index]
                            var end = start
                            var len = 0
                            while (end < v.length && len + v[end].len() <= width) {
                                len += v[end].len()
                                end++
                            }
                            if (len > 0) {
                                val c = TextCell(
                                    v.substring(start, end),
                                    column.style,
                                    column.align,
                                    OriginSupply,
                                    column.weight
                                )
                                writeColumn(c, rect, row.gap)
                                columnsPos[index] = end
                            } else {
                                val c = TextCell(
                                    " ",
                                    column.style,
                                    Align.FILL,
                                    OriginSupply,
                                    column.weight
                                )
                                writeColumn(c, rect, row.gap)
                            }
                        }
                        is ImageCell -> {
                            writeColumn(column, rect, row.gap)
                            columnsPos[index] = -1
                        }
                        is CommandCell->writeBytes(column.getValue(),false)
                    }
                }
            }
        }
        writeLf()
        return height
    }

    /**
     * 写入非受限列宽度受限[row]行数据
     */
    protected open fun writeUnLimitRow(row: Row) {
        val rs = getRowRect(row)
        for ((index, column) in row.columns.withIndex()) {
            writeColumn(column, rs[index], row.gap)
        }
        writeLf()
    }

    /**
     * 根据[column]列写入格式和内容 [rect]写入区域
     * @return 是否写入打印机指令 true下一行需要清除特殊指令
     */
    protected fun writeColumn(column: Cell<*>, rect: Rect, gap: Int) {
        return when (column) {
            is TextCell -> {
                if (column.style.bold) {
                    writeBold()
                }
                if (column.style.double) {
                    writeHeighten()
                }
                if (column.style.underLine) {
                    writeUnderLine()
                }
                writeColumnContent(column, rect, gap)
            }
            is ImageCell -> {
                when (column.type) {
                    is BarCode  -> writeBarCode(column.content,column.type.type, column.width,column.height)
                    QRCode -> writeQrCode(
                        column.content,
                        column.width,column.height
                    )
                    Image -> {
                        val b = column.getValue()
                        writeBitmap(BitmapFactory.decodeByteArray(b, 0, b.size),column.width,column.height)
                    }
                    else -> throw IllegalStateException("not support yet")
                }
                if (column.description.isNotEmpty()){
                    writeLf()
                    writeBytes(column.description.toByteArray())
                    writeLf()
                } else {

                }
            }
            is CommandCell->writeBytes(column.getValue(),false)
        }
    }

    /**
     * 根据对齐格式写入内容
     * @param column 文本列
     * @param rect 区域限制
     * @return
     */
    protected fun writeColumnContent(column: TextCell, rect: Rect, gap: Int) {
        val v = column.getValue()
        val writeLen = v.toCharArray().fold(0) { acc, c ->
            acc + c.len()
        }
        val colLeft = rect.left
        val left = this.left
        //调整光标到开始位置
        if (left < colLeft) {
            writeChar(' ', colLeft - left)
        } else if (left >= colLeft && left > 0) {
            writeChar(' ', WORD)
        }
        val width = rect.right - this.left
        when (column.align) {
            Align.LEFT -> {
                writeBytes(v.toByteArray(charset))
                if (writeLen >= width + gap) {
                    writeLf()
                } else {
                    writeChar(' ', width - writeLen)
                }
            }
            Align.CENTER -> {
                if (writeLen < width + gap) {
                    val padding = (width - writeLen) / 2
                    writeChar(' ', padding)
                    writeBytes(v.toByteArray(charset))
                    writeChar(' ', padding)
                } else {
                    writeBytes(v.toByteArray(charset))
                    writeLf()
                }
            }
            Align.RIGHT -> {
                if (writeLen < width + gap) {
                    val padding = (width - writeLen)
                    writeChar(' ', padding)
                    writeBytes(v.toByteArray(charset))
                } else {
                    writeBytes(v.toByteArray(charset))
                    writeLf()
                }
            }
            Align.FILL -> {
                var index = 0
                var writeLeft = this.left
                while (writeLeft < rect.right) {
                    writeLeft += v[index % v.length].len()
                    writeChar(v[index % v.length], 1)
                    index++
                }
            }
        }
    }

    /**
     * 写入[value]字节数组，[moveLeft]是否移动光标，写入指令不需要移动
     */
    protected fun writeBytes(value: ByteArray, moveLeft: Boolean = true) {
        writer.write(value)
        if (moveLeft) {
            moveLeft(value.size)
        }
    }

    /**
     * 写入[num]个字节
     */
    protected fun writeChar(char: Char, num: Int = 1) = repeat(num) { writeChar(char, true) }

    /**
     * 写入[value]字节，[moveLeft]是否移动光标，写入指令不需要移动
     */
    protected fun writeChar(value: Char, moveLeft: Boolean = true) {
        writer.write(value.toInt())
        if (moveLeft) {
            moveLeft(value.len())
        }
    }

    /**
     * 根据宽度[len]获取受限列[column]行数
     */
    protected fun getHeight(column: TextCell, len: Int): Int {
        val v = column.getValue()
        var height = 1
        var l = 0
        for (c in v.toCharArray()) {
            if (l + c.len() > len) {
                l = 0
                height++
            }
            l += c.len()
        }
        return height
    }

    /**
     * 字节占位，ascii占用1位，其他为2位
     */
    private fun Char.len(): Int = if (this.toInt() in 32..126) {
        1
    } else {
        2
    }

    /**
     * 检测格式并生成打印内容
     */
    override fun printData(stream: OutputStream, inputStream: InputStream): Pair<Boolean, String> {
        rows.forEach { row ->
            printerType.checkRowIllegal(row)
            writer.reset()
            writeRow(row)
            stream.write(writer.toByteArray())
            val cell=row.columns.firstOrNull()
            if (cell is CommandCell&&cell.outData.isNotEmpty()){
                inputStream.read(cell.outData)
            }
        }
        return true to "打印成功"
    }

    /**
     * 检测格式并生成预览内容
     */
    override fun preview(styles: List<String>): CharSequence {
        rows.forEach {
            printerType.checkRowIllegal(it)
        }
        return HtmlWriter(rows).preview(styles)
    }
}