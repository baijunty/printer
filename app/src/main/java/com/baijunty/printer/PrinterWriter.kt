package com.uplus.printer

/**
 *打印机打印内容生成
 */

interface PrinterWriter {
    /**
    * @param
    * @return 需要写入的字节
    */
    fun print():ByteArray

    /**
    * @param
    * @return 生成的预览数据
    */
    fun preview():CharSequence
}