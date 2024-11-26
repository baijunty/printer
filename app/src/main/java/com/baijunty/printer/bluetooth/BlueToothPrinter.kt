package com.baijunty.printer.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import com.baijunty.printer.*
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

/**
 * 目的蓝牙打印机
 * [address]地址,类型,[printerWriter]生成打印数据
 * @property [writer]用于自定义设置[PrinterWriter]
 */

open class BlueToothPrinter(
    printerWriter: PrinterWriter
) : AbstractSocketPrinter(printerWriter) {

    companion object {
        @JvmStatic
        val BLUETOOTH_PRINTER = BlueToothPrinter(
            CommonBluetoothWriter(
                Type.Type58, Charset.defaultCharset(),
                emptyList()
            )
        )
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
            require(row.columns.isNotEmpty()) { "内容不能为空" }
            require(row.gap >= WORD) { "间距不能小于等于0" }
            require(!row.columns.any { it.weight < 1 }) { "列宽不能小于等于2" }
            if (row.columns.size > 1) {
                row.columns.forEach {
                    when (val define = it) {
                        is TextCell -> require(!(define.style.double || define.style.bold)) { "蓝牙打印机加粗加高条只支持单行" }
                        is ImageCell -> require(row.columns.size <= 1) { "图片二维码条码打印只支持单行" }
                        is CommandCell -> {}
                    }
                }
            }
        }

        /**
         * @param
         * @return 打印机类型支持最大列数
         */
        fun getMaxColumns(): Int = when (this) {
            Type58 -> 5
            Type80 -> 7
            Type110 -> 10
        }

        fun getImageWidth(): Int = when (this) {
            Type58 -> 384
            Type80 -> 528
            Type110 -> 704
        }

        /**
         * @param
         * @return 是否超过最大列支持
         */
        fun isColumnsOutLimit(columnNum: Int): Boolean {
            val maxColumn = getMaxColumns()
            return columnNum > maxColumn
        }
    }

    override fun createSocket() {
    }

    /**
     * 释放端口连接
     */
    protected fun releaseSocket() {
        synchronized(BlueToothPrinter::class.java) {
            if (_socket?.isConnected == true) {
                runCatching {
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
    @SuppressLint("NewApi", "MissingPermission")
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
    protected val socket: BluetoothSocket
        @SuppressLint("MissingPermission")
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
     * 生命周期管理，active结束时断开连接
     */
    override fun close() {
        releaseSocket()
    }

    override fun getOutputStream(): OutputStream = DelegateOutputStream(socket.outputStream)

    override fun getInputStream(): InputStream = DelegateInputStream(socket.inputStream)
}