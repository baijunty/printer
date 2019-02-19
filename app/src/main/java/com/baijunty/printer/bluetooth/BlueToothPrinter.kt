package com.uplus.printer.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.uplus.printer.*
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates

/**
 * 目的蓝牙打印机
 * [address]地址,类型,[printerWriter]生成打印数据,[activity]生成内容预览
 * @property [writer]用于自定义设置[PrinterWriter]
 */

class BlueToothPrinter private constructor(var printerWriter: PrinterWriter
): PrintWorkModel{
    private lateinit var printerHandler:Handler
    init {
        thread (isDaemon=true,name = "bluetooth printer thread"){
            Looper.prepare()
            printerHandler=Handler(Looper.myLooper())
            Looper.loop()
        }
    }
    companion object {
        @JvmStatic
        val BLUETOOTH_PRINTER=BlueToothPrinter(CommonBluetoothWriter(Type.Type58, Charset.defaultCharset(),
            emptyList()))
    }

    private var printTask:PrintTask?=null

    var address:String by Delegates.observable(""){_,o,n->
        if (o!=n&&BluetoothAdapter.checkBluetoothAddress(o)){
            releaseSocket()
        }
    }

    override var writer: PrinterWriter
        get() = printerWriter
        set(value) {
            printerWriter=value
        }
    /**
     * 打印机类型
     */
    enum class Type(val len:Int){
        Type58(32),Type80(48),Type110(68);

        /**
        * @param row 检测行格式是否正确支持
        * @return
         * @throws IllegalArgumentException 格式不支持或设置异常
        */
        fun checkRowIllegal(row: Row){
            val size=row.columns.size
            if (isColumnsOutLimit(size)){
                throw IllegalArgumentException("列数大于打印机支持最大列${getMaxColumns()}")
            }
            if (row.columns.isEmpty()){
                throw IllegalArgumentException("内容不能为空")
            }
            if (row.gap< WORD){
                throw IllegalArgumentException("间距不能小于等于0")
            }
            if (row.columns.any { it.weight<1 }){
                throw IllegalArgumentException("列宽不能小于等于2")
            }
            if (row.columns.size>1){
                row.columns.forEach {
                    val define=it
                    when(define){
                        is TextCell->if (define.style.double||define.style.bold){
                            throw IllegalArgumentException("蓝牙打印机加粗加高条只支持单行")
                        }
                        is ImageCell -> if (row.columns.size>1){
                            throw IllegalArgumentException("图片二维码条码打印只支持单行")
                        }
                    }
                }
            }
            val leftSpace=len-(row.gap)*(size-1)
            val totalWeight=row.columns.fold(0){acc, column -> acc+column.weight }
            val avgColLen=leftSpace.toFloat()/totalWeight.toFloat()
            if (row.columns.minBy { it.weight }!!.weight*avgColLen< WORD){
                throw IllegalArgumentException("列宽不能小于等于2")
            }
        }

        /**
        * @param
        * @return 打印机类型支持最大列数
        */
        private fun getMaxColumns():Int=when(this){
            BlueToothPrinter.Type.Type58 -> 5
            BlueToothPrinter.Type.Type80 -> 7
            BlueToothPrinter.Type.Type110 -> 10
        }

        /**
        * @param
        * @return 是否超过最大列支持
        */
        private fun isColumnsOutLimit(columnNum:Int):Boolean{
            val maxColumn=getMaxColumns()
            return columnNum>maxColumn
        }
    }


    /**
     * 释放端口连接
     */
    private fun releaseSocket() {
        synchronized(BlueToothPrinter::class.java) {
            if (_socket?.isConnected == true) {
                kotlin.runCatching {
                    _socket!!.outputStream.close()
                    _socket!!.inputStream.close()
                    _socket!!.close()
                }
                _socket = null
            }
        }
    }

    //端口
    private var _socket: BluetoothSocket? = null

    //使用端口
    private val socket: BluetoothSocket
        get() {
            if (_socket == null || !_socket!!.isConnected) {
                _socket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address).createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                _socket!!.connect()
            }
            return _socket!!
        }

    protected fun finalize() {
        releaseSocket()
    }

    /**
     ** 正式打印
     ** @param context
    * @param listener 打印任务结束回调
    * @return
    */
    override fun print(context: Context,listener: PrinterListener) {
        cancel()
        printTask=PrintTask(context,listener)
        printerHandler.post(printTask)
    }

    /**
     * [context]生成预览数据View
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun preview(context: Context ): View{
        val view= WebView(context)
        val settings = view.settings;
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.allowContentAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.javaScriptEnabled = true;
        settings.setSupportZoom(true)
        view.loadDataWithBaseURL(null,writer.preview().toString(),"text/HTML", "UTF-8", null)
        return view
    }

    /**
     * 生命周期管理，active结束时断开连接
     */
    override fun close() {
        cancel()
    }

    override fun cancel() {
        releaseSocket()
        printTask?.let {
            printerHandler.removeCallbacks(it)
        }
    }

    private inner class PrintTask(val context: Context, val listener: PrinterListener): Runnable {
        override fun run() {
            val r=kotlin.runCatching {
                Log.d(address,writer.preview().toString())
                socket.outputStream.write(writer.print())
            }
            if (context is Activity&&!context.isFinishing){
                context.runOnUiThread {
                    listener.onFinish(r.isSuccess,r.exceptionOrNull())
                }
            } else {
                Handler(context.mainLooper).post {
                    listener.onFinish(r.isSuccess,r.exceptionOrNull())
                }
            }
        }
    }

}