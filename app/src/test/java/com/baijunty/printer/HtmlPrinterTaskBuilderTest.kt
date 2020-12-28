package com.baijunty.printer

import org.junit.Test

internal class HtmlPrinterTaskBuilderTest {
    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
    }


    @Test
    fun test(){
        val builder= HtmlPrinterTaskBuilder()
        builder.line("werwewerwe",bold = true,align = Align.CENTER)
        val task=builder
            .newLine {
                string("1")
                string("2")
                string("sdfsdfdsfsdfwerwrewerwerewrewr")
            }
            .newLine {
                string("1")
                string("2")
                string("sdfsdfdsfsdfwerwrewerwerewrewr")
            }
            .build()
        val result=task.writer.preview()
        print(result)
    }
}