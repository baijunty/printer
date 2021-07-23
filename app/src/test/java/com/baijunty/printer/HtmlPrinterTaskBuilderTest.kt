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
        val s=JolimarkPrinterTaskBuilder("192.168.2.107")
            .setConnectType(ConnectTypeEnum.BLUETOOTH)
            .line("hello,world", bold = true, heighten = true, underLine = true, align = Align.CENTER)
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .divider('=')
            .newLine {
                string("名称",weight = 3)
                string("编码")
                string("单位")
                string("价格")
                string("金额")
            }
            .divider('=')
            .newLine {
                string("名称",weight = 3)
                string("编码")
                string("单位")
                string("价格")
                string("金额")
            }
            .newLine {
                string("名称",weight = 3)
                string("编码")
                string("单位")
                string("价格")
                string("金额")
            }
            .divider('-')
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .line("往来单位：123234534")
            .forward(4)
            .build().writer.preview()
        println(s)
    }
}