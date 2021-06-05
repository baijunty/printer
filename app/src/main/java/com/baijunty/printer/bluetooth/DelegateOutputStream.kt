package com.baijunty.printer.bluetooth

import java.io.InputStream
import java.io.OutputStream

class DelegateOutputStream(private val delegate: OutputStream): OutputStream() {
    override fun write(b: Int) = delegate.write(b)
}