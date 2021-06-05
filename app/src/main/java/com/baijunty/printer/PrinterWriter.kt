package com.baijunty.printer

import java.io.InputStream
import java.io.OutputStream

/**
 *打印机打印内容生成
 */

interface PrinterWriter {
    /**
    * @param stream 写入流
     * @param inputStream 读入流
    */
    fun printData(stream:OutputStream,inputStream: InputStream)

    /**
    * @param
    * @return 生成的预览数据
    */
    fun preview():CharSequence
}