package com.baijunty.printer

import com.baijunty.printer.EncryptUtil.md5
import org.junit.Test
import java.text.SimpleDateFormat
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
        val d=Date().apply {
            time=System.currentTimeMillis()
        }
        val t=SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d)
        println(t)
        println("".md5())
    }
}