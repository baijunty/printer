package com.baijunty.printer.bluetooth

import android.graphics.*
import com.baijunty.printer.BarCodeType
import com.baijunty.printer.Row
import com.baijunty.printer.toQrCodeBitmap
import java.nio.charset.Charset
import kotlin.math.min

/**
 * 市面常用打印机指令
 */
open class CommonBluetoothWriter(type: BlueToothPrinter.Type, charset: Charset, rows:List<Row>):
    BlueToothWriter(type,charset,rows) {
    companion object {
        /**
         * 讲图片[bmpOriginal]按宽度[targetWidth]转换成打印机支持的灰度图
        */
         fun toGrayScale(bmpOriginal: Bitmap, targetWidth: Int): Bitmap {
            val height = bmpOriginal.height
            val width = bmpOriginal.width
            var bmpGraysSale = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888)
            val c = Canvas(bmpGraysSale)
            val paint = Paint()
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            val f = ColorMatrixColorFilter(cm)
            paint.colorFilter = f
            c.drawBitmap(bmpOriginal, 0f, 0f, paint)
            if (width!=targetWidth){
                val m = Matrix()
                val scale = targetWidth.toFloat() / width.toFloat()
                m.postScale(scale, scale)
                bmpGraysSale = Bitmap.createBitmap(bmpGraysSale, 0, 0, bmpGraysSale.width, bmpGraysSale.height, m, true)
            }
            return bmpGraysSale
        }
    }

    /**
     * 加粗指令
     */
    override fun writeBold()= writeBytes(byteArrayOf(0x1b, 0x21, 0x08),false)
    /**
     * 换行
     */
    override fun writeLf(){
        writeBytes(byteArrayOf(0x0d, 0x0a),false)
        left=0
    }
    /**
     * 倍高
     */
    override fun writeHeighten() = writeBytes(byteArrayOf(0x1b, 0x40, 0x1d, 0x21, 0x01),false)
    /**
     * 生成[v]二维码图片后打印
     */
    override fun writeQrCode(v: String, width:Int, height:Int) {
        val w=if (width<=0) printerType.getImageWidth() else min(width,printerType.getImageWidth())
        val h=if (height<=0) w else height
        val useSize= min(w,h)
        writeBitmap(toQrCodeBitmap(v,useSize,useSize),useSize,useSize)
    }

    override fun writeBitmap(bitmap: Bitmap, width: Int, height: Int) {
        writeCenter()
        val mode=0
        val len= if (width*height>100) min(printerType.getImageWidth(),width) else printerType.getImageWidth()
        val scaleWidth= min(((bitmap.width+7)/8)*8,len)
        val printBitmap = toGrayScale(bitmap, scaleWidth)
        val width = printBitmap.width
        val data = ByteArray(width * printBitmap.height)
        val pixels = IntArray(data.size)
        printBitmap.getPixels(pixels, 0,width, 0, 0, width, printBitmap.height)
        pixels.foldIndexed(data) { i, b, p ->
            //转换为YUV格式亮度后比较
            b[i] = if (Color.red(p) * 0.299 + 0.587 * Color.green(p) + 0.114 * Color.blue(p) < 128) 1 else 0
            b
        }
        val lineNum = width / 8
        val height = data.size / width
        val command = ByteArray(8)
        command[0] = 0x1d
        command[1] = 0x76
        command[2] = 0x30
        command[3] = mode.toByte()
        command[4] = (lineNum and 0xff).toByte()
        command[5] = ((lineNum ushr 8) and 0xff).toByte()
        command[6] = (height and 0xff).toByte()
        command[7] = ((height ushr 8) and 0xff).toByte()
        var k = 0
        val b = ByteArray(lineNum)
        writeBytes(command,false)
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
        writeLf()
        clean()
    }
    /**
     * 打印[v]条形码
     */
    override fun writeBarCode(v: String, type: BarCodeType, width: Int, height: Int) {
        writeCenter()
        val contentByte = v.toByteArray(charset)
        if (type.value < 65) {
            writeBytes(byteArrayOf(0x1d,0x6b,type.value.toByte()),false)
            writeBytes(contentByte,false)
            writeBytes(byteArrayOf(0x0),false)
        } else {
            val len = contentByte.size
            writeBytes(byteArrayOf(0x1d,0x6b,type.value.toByte(),len.toByte()),false)
            writeBytes(contentByte,false)
        }
        writeLf()
        clean()
        writeLf()
    }

    /**
     * 切纸
     * 大部分都不支持
     */
    override fun cutPaper()= writeBytes(byteArrayOf(0x1d,0x56,0x42,0x64.toByte()),false)

    /**
     * 清空打印格式
     */
    override fun clean() = writeBytes(byteArrayOf(0x1b, 0x40),false)

    /**
     * 下划线
     */
    override fun writeUnderLine()=writeBytes(byteArrayOf(0x1c, 0x2d, 0x2.toByte()),false)
    /**
     * 居中
     */
    override fun writeCenter() =writeBytes(byteArrayOf(0x1b, 0x61, 0x1.toByte()),false)
}