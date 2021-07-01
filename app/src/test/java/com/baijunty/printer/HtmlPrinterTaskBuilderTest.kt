package com.baijunty.printer

import com.baijunty.printer.jolimark.enums.ConnectTypeEnum
import com.baijunty.printer.jolimark.enums.PrinterEnum
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

internal class HtmlPrinterTaskBuilderTest {
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
    }


    @Test
    fun test(){
        val sb=ByteArrayOutputStream()
        val s=BlueToothPrinterTaskBuilder("3C:71:BF:FB:60:DA")
            .line("hello,world", bold = true, heighten = true, underLine = true, align = Align.CENTER)
            .newLine {
                string("名称")
                string("编码")
                string("编码",weight = 2)
                string("问好",weight = 3)
            }
            .newLine {
                string("名称")
                string("编码")
                string("编码",weight = 2)
                string("问好",weight = 3)
            }
            .build().writer.printData(sb,ByteArrayInputStream(ByteArray(0)))
        File("d://test.html").writeBytes(sb.toByteArray())
    }
}