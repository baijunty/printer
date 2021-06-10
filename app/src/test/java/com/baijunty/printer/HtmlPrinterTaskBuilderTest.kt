package com.baijunty.printer

import org.junit.Test
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
        val b=Base64.getDecoder().decode(File("d://5003.txt").readText())
        println(b.size)
        File("d://test.jpg").writeBytes(b)
    }
}