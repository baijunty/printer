package com.baijunty.printer.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.view.View
import android.webkit.WebView
import com.baijunty.printer.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.nio.charset.Charset
import java.util.*
import kotlin.properties.Delegates

/**
 * 目的蓝牙打印机
 * [address]地址,类型,[printerWriter]生成打印数据
 * @property [writer]用于自定义设置[PrinterWriter]
 */

class BlueToothPrinter private constructor(
    var printerWriter: PrinterWriter
) : PrintWorkModel {
    var printTime: Int = 1

    companion object {
        @JvmStatic
        val BLUETOOTH_PRINTER = BlueToothPrinter(
            CommonBluetoothWriter(
                Type.Type58, Charset.defaultCharset(),
                emptyList()
            )
        )
    }

    var address: String by Delegates.observable("") { _, o, n ->
        if (o != n && BluetoothAdapter.checkBluetoothAddress(n)) {
            releaseSocket()
        }
    }

    override var writer: PrinterWriter
        get() = printerWriter
        set(value) {
            printerWriter = value
        }

    /**
     * 打印机类型
     */
    enum class Type(val len: Int) {
        Type58(32), Type80(48), Type110(68);

        /**
         * @param row 检测行格式是否正确支持
         * @return
         * @throws IllegalArgumentException 格式不支持或设置异常
         */
        fun checkRowIllegal(row: Row) {
            val size = row.columns.size
            require(!isColumnsOutLimit(size)) { "列数大于打印机支持最大列${getMaxColumns()}" }
            require(row.columns.isNotEmpty()) { "内容不能为空" }
            require(row.gap >= WORD) { "间距不能小于等于0" }
            require(!row.columns.any { it.weight < 1 }) { "列宽不能小于等于2" }
            if (row.columns.size > 1) {
                row.columns.forEach {
                    when (val define = it) {
                        is TextCell -> require(!(define.style.double || define.style.bold)) { "蓝牙打印机加粗加高条只支持单行" }
                        is ImageCell -> require(row.columns.size <= 1) { "图片二维码条码打印只支持单行" }
                    }
                }
            }
            val leftSpace = len - (row.gap) * (size - 1)
            val totalWeight = row.columns.fold(0) { acc, column -> acc + column.weight }
            val avgColLen = leftSpace.toFloat() / totalWeight.toFloat()
            require(row.columns.minBy { it.weight }!!.weight * avgColLen >= WORD) { "列宽不能小于等于2" }
        }

        /**
         * @param
         * @return 打印机类型支持最大列数
         */
        private fun getMaxColumns(): Int = when (this) {
            Type58 -> 5
            Type80 -> 7
            Type110 -> 10
        }

        /**
         * @param
         * @return 是否超过最大列支持
         */
        private fun isColumnsOutLimit(columnNum: Int): Boolean {
            val maxColumn = getMaxColumns()
            return columnNum > maxColumn
        }
    }


    /**
     * 释放端口连接
     */
    private fun releaseSocket() {
        synchronized(BlueToothPrinter::class.java) {
            if (_socket?.isConnected == true) {
                runCatching {
                    _socket!!.outputStream.close()
                    _socket!!.inputStream.close()
                    _socket!!.close()
                }
                _socket = null
            }
        }
    }

    /**
     * 配对设备
     * @return true 成功
     */
    @SuppressLint("NewApi")
    fun pairToDevices(device: BluetoothDevice): Boolean {
        return when {
            device.bondState == BluetoothDevice.BOND_BONDED -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> device.createBond()
            else -> {
                val blueToothClass = device.javaClass
                val createBondMethod = blueToothClass.getDeclaredMethod("createBond")
                createBondMethod.isAccessible = true
                createBondMethod.invoke(device)
                return true
            }
        }
    }

    //端口
    private var _socket: BluetoothSocket? = null

    //使用端口
    private val socket: BluetoothSocket
        get() {
            if (_socket == null || !_socket!!.isConnected) {
                val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
                if (pairToDevices(device)) {
                    _socket =
                        device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    _socket!!.connect()
                } else {
                    throw  IllegalStateException("设备配对失败")
                }
            }
            return _socket!!
        }

    protected fun finalize() {
        releaseSocket()
    }

    /**
     ** 正式打印
     ** @param context
     * @return
     */
    override fun print(context: Context): Observable<Boolean> {
        return Observable.just(writer)
            .map {
                for (i in 0 until printTime) {
                    if (!tryWrite()) {
                        releaseSocket()
                        socket.outputStream.write(it.print())
                    }
                }
                true
            }.subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * [context]生成预览数据View
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun preview(context: Context): Observable<View> {
        return Observable.just(writer)
            .map { it.preview().toString() }
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                val view = WebView(context)
                val settings = view.settings
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.allowContentAccess = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.javaScriptEnabled = true
                settings.setSupportZoom(true)
                view.loadDataWithBaseURL(null, it, "text/HTML", "UTF-8", null)
                view as View
            }.subscribeOn(Schedulers.single())
    }

    /**
     * 生命周期管理，active结束时断开连接
     */
    override fun close() {
        releaseSocket()
    }

    private fun tryWrite(): Boolean {
        return runCatching {
            socket.outputStream.write(writer.print())
            true
        }.getOrDefault(false)
    }

}