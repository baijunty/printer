package com.baijunty.printer

import android.graphics.Bitmap

/**
 * 用于设置打印机支持指令格式
 */
interface FormatWriter {
    /**
     * 加粗指令或格式
    */
    fun writeBold()
    /**
     * 换行
    * @param
    * @return
    */
    fun writeLf()
    /**
     * 字体增大
    * @param
    * @return
    */
    fun writeHeighten()

    /**
     * 图片写入
    * @param bitmap 待写入图片
    * @return
    */
    fun writeBitmap(bitmap: Bitmap,width:Int,height:Int)
    /**
    * 二维码写入
     * @param v 二维码内容
    * @return
    */
    fun writeQrCode(v: String,width:Int,height:Int)
    /**
     * 条码生成写入
    * @param v 条码内容
    * @return
    */
    fun writeBarCode(v: String,type:BarCodeType,width:Int,height:Int)
    /**
     * 下划线
    * @param
    * @return
    */
    fun writeUnderLine()

    /**
     * 蓝牙打印机切纸
    * @param
    * @return
    */
    fun cutPaper()

    /*** 清除格式设置
    * @param
    * @return
    */
    fun clean()

    /**
     * 设置居中
     */
    fun writeCenter()
}