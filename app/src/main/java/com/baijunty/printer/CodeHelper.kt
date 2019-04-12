package com.baijunty.printer

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.baijunty.printer.oned.Code128Writer
import com.baijunty.printer.oned.Code93Writer
import com.baijunty.printer.qrcode.QRCodeWriter

/**
 * 生成二维码图片
 * @param v 二维码内容
 * @param width 图片宽
 * @param height 图片高
 * @return 生成的二维码图片
 */
fun toQrCodeBitmap(v:String,width: Int=400,height: Int=400):Bitmap{
    val result= QRCodeWriter().encode(v, width,height)
    val width = result.width
    val height = result.height
    val pixels=IntArray(width*height)
    for (y in 0 until height) {
        val offset=width*y
        for (x in 0 until width) {
            pixels[offset+x] = if (result.get(x, y)) BLACK else WHITE
        }
    }
    return  Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels,0,width,0,0,width,height)
    }
}

/**
 ** 条形码
* @param v 条形码内容
 * @param code128 使用CODE128编码 默认true
 * @param width 条码宽度 默认400
 * @param height 条码高度 默认200
* @return 条形码图形
*/
fun toBarCodeBitmap(v:String,code128:Boolean=true,width:Int=400,height:Int=200):Bitmap{
    val result= if (code128) Code128Writer() else Code93Writer()
    val bitMatrix=result.encode(v,width,height)
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (bitMatrix.get(x, y)) {
                pixels[y * width + x] = BLACK
            } else {
                pixels[y * width + x] = WHITE
            }
        }
    }
    return  Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels,0,width,0,0,width,height)
    }
}