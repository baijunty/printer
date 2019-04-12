package com.baijunty.printer.bluetooth

import com.baijunty.printer.FormatWriter
import com.baijunty.printer.PrinterWriter
import com.baijunty.printer.Row
import java.nio.charset.Charset

abstract class ContentWriter(protected val printerType: BlueToothPrinter.Type, protected val charset: Charset, protected val rows: List<Row>):
    PrinterWriter,
    FormatWriter