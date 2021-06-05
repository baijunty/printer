package com.baijunty.printer.bluetooth

import java.io.InputStream

class DelegateInputStream(private val delegate:InputStream):InputStream() {
    override fun read(): Int = delegate.read()

    override fun reset() = delegate.reset()

    override fun markSupported(): Boolean = delegate.markSupported()

    override fun mark(readlimit: Int) = delegate.mark(readlimit)

    override fun available(): Int = delegate.available()

}