package com.baijunty.printer.bluetooth

import android.graphics.*
import android.util.Log
import com.baijunty.printer.*
import com.baijunty.printer.bluetooth.CommonBluetoothWriter.Companion.toGrayScale
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.math.max
import kotlin.math.min
/**
 * 佳博打印机指令
 * SIZE 80 mm, 80 mm 先设置打印区域
GAP 0 mm, 0 mm 间距
CLS 清空缓存区
TEXT 0,0,"TSS24.BF2",0,1,1,""
TEXT 0,0,"TSS24.BF2",0,1,1,"往来单位:京克隆超市"
TEXT 0,35,"TSS24.BF2",0,1,1,"单据编号:XSD-2018-12-17-00001"
TEXT 0,70,"TSS24.BF2",0,1,1,"单据日期:2018-12-17"
TEXT 0,105,"TSS24.BF2",0,1,1,"出库仓库:食品类货架1-1"
TEXT 0,245,"TSS24.BF2",0,1,1,"================================"
TEXT 0,280,"TSS24.BF2",0,1,1,"名称  数量  单位  折后单价  折后金额"
TEXT 0,315,"TSS24.BF2",0,1,1,"================================"
TEXT 0,350,"TSS24.BF2",0,1,1,"金防  20.00 包    20.00     400.00"
TEXT 0,630,"TSS24.BF2",0,1,1,"--------------------------------"
TEXT 0,665,"TSS24.BF2",0,1,1,"总数量:105.00"
TEXT 0,700,"TSS24.BF2",0,1,1,"总金额:16500.00"
TEXT 0,980,"TSS24.BF2",0,1,1,"打印日期:2018-12-17 11:14:12"
PRINT 1 打印份数
 */
open class GprinterWriter(type: BlueToothPrinter.Type, charset: Charset, rows:List<Row>): BlueToothWriter(type,charset,rows) {

    protected fun writeCommand(command:String){
        writeBytes(command.toByteArray(charset),false)
    }

    protected fun getSize():Int=when(printerType){
        BlueToothPrinter.Type.Type58 -> 58
        BlueToothPrinter.Type.Type80 -> 80
        BlueToothPrinter.Type.Type110 -> 110
    }

    protected  open fun initPrinter(height: Int,cut:Boolean=false) {
        val size=getSize()
        var str = "SIZE $size mm,${height} mm\r\n"
        writeCommand(str)
        str = "GAP 0 mm,0 mm\r\n"
        writeCommand(str)
        if (cut){
            str="SET CUTTER 1\r\n"
            writeCommand(str)
        }
        str = "CLS\r\n"
        writeCommand(str)
        clean()
        top =0
    }

    override fun clean() {
    }

    override fun cutPaper() {
    }

    override fun writeBarCode(v: String, type: BarCodeType, width: Int, height: Int) {
        val h=if (height<0) 96 else height
        val str = "BARCODE 24,$top,\"${type}\",$h,1,0,2,4,\"$v\"\r\n"
        writeCommand(str)
    }

    override fun writeBitmap(bitmap: Bitmap, width: Int, height: Int) {
        val len= if (width*height>100) min(printerType.getImageWidth(),width) else printerType.getImageWidth()
        val scaleWidth= min(((bitmap.width+7)/8)*8,len)
        val printBitmap = toGrayScale(bitmap, scaleWidth)
        val width = printBitmap.width
        val data = ByteArray(width * printBitmap.height)
        val pixels = IntArray(data.size)
        printBitmap.getPixels(pixels, 0,width, 0, 0, width, printBitmap.height)
        pixels.foldIndexed(data) { i, b, p ->
            //转换为YUV格式亮度后比较
            b[i] = if (Color.red(p) * 0.299 + 0.587 * Color.green(p) + 0.114 * Color.blue(p) < 128) 0 else 1
            b
        }
        val lineNum = width / 8
        val height = data.size / width
        var k = 0
        val b = ByteArray(lineNum)
        val str = "BITMAP 0,$top,$lineNum,$height,0,"
        writeCommand(str)
        for (i in 0 until height) {
            for (j in 0 until lineNum) {
                b[j] = (0 until 8).foldIndexed(0) { index, acc, _ ->
                    val d = data[k + index].toInt()
                    acc + (d shl (7-index))
                }.toByte()
                k += 8
            }
            writeBytes(b,false)
        }
    }

    override fun writeBold() {
    }

    override fun writeCenter() {
    }

    override fun writeHeighten() {
    }

    override fun writeRow(row: Row) {
        val cell=row.columns[0]
        val isText=cell is TextCell
        val heighten:Int
        if (isText){
            val textCell=cell as TextCell
            heighten=if(textCell.style.double) 2 else 1
            val str = "TEXT 0,$top,\"TSS24.BF2\",0,1,$heighten,\""
            writeCommand(str)
        }
        super.writeRow(row)
        if (isText){
            writeCommand("\"\r\n")
        }
    }



    override fun writeLf() {
        writeBytes(byteArrayOf(0x0d, 0x0a),false)
    }

    override fun writeQrCode(v: String, width: Int, height: Int) {
        val str = "QRCODE 0,$top,L,8,A,0,\"$v\"\r\n"
        writeCommand(str)
    }

    override fun writeUnderLine() {
    }

    protected open fun finish(){
        val str = "PRINT 1\r\n"
        writeCommand(str)
    }

    protected fun calcuteTotalHeight():Int{
        return rows.fold(0){acc, row ->
            acc+getRowHeight(row)
        }
    }

    protected fun getImageCellHeight(cell: ImageCell):Int{
        return when(cell.type) {
            is BarCode-> if (cell.height<0) 15 else cell.height/8+3
            QRCode -> 27
            Image -> {
                val b = cell.getValue()
                val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                val len = if (cell.width*cell.height>100) min(printerType.getImageWidth(),cell.width) else printerType.getImageWidth()
                val scaleWidth = min(((bitmap.width + 7) / 8) * 8, len)
                val printBitmap = toGrayScale(bitmap, scaleWidth)
                printBitmap.height/8
            }
        }
    }

    protected fun getRowHeight(row: Row):Int{
        val rs=getRowRect(row)
        return if (row.rangeLimit){
            var height=3
            for ((index, column) in row.columns.withIndex()) {
                height = when(column){
                    is TextCell ->
                        max(height, (getHeight(column, rs[index].width())+if (column.style.double) 1 else 0)*3)
                    is ImageCell ->getImageCellHeight(column)
                    is CommandCell -> 0
                }
            }
            height
        } else {
            var height=3
            for ((index, column) in row.columns.withIndex()) {
                height=when(column){
                    is TextCell ->{
                        height+(getHeight(column, rs[index].width())-1+if (column.style.double) 1 else 0)*3
                    }
                    is ImageCell ->getImageCellHeight(column)
                    is CommandCell -> 0
                }
            }
            height
        }
    }

    override fun printData(stream: OutputStream, inputStream: InputStream): Boolean {
        rows.forEach {
            printerType.checkRowIllegal(it)
            initPrinter(getRowHeight(it))
            writer.reset()
            writeRow(it)
            stream.write(writer.toByteArray())
            finish()
            val cell=it.columns.firstOrNull()
            if (cell is CommandCell&&cell.outData.isNotEmpty()){
                inputStream.read(cell.outData)
            }
        }
        return true
    }
}