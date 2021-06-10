package com.baijunty.printer.net

import com.baijunty.printer.AbstractSocketPrinter
import com.baijunty.printer.PrinterWriter
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class LanPrinter(
    printerWriter: PrinterWriter, private val port:Int
) : AbstractSocketPrinter(printerWriter) {
    private var _socket:Socket?=null

    private val socket:Socket
        get() {
            if (_socket==null||_socket!!.isClosed){
                _socket=Socket()
            }
            return _socket!!
        }

    override fun createSocket() {
        socket.soTimeout=1000*50
        socket.connect(InetSocketAddress(address,port))
    }

    override fun getOutputStream(): OutputStream {
        return socket.getOutputStream()
    }

    override fun getInputStream(): InputStream {
        return socket.getInputStream()
    }

    override fun close() {
        if (_socket?.isClosed==false){
            _socket?.close()
        }
        _socket=null
    }
}