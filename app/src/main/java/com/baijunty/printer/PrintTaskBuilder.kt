package com.baijunty.printer

import android.annotation.TargetApi
import android.os.Build
import com.baijunty.printer.bluetooth.BlueToothPrinter
import com.baijunty.printer.bluetooth.CommonBluetoothWriter
import com.baijunty.printer.html.HtmlPrinter
import com.baijunty.printer.html.HtmlWriter
import com.baijunty.printer.jolimark.JolimarkBluetoothPrinterWriter
import com.baijunty.printer.jolimark.JolimarkHttpJsonWriter
import com.baijunty.printer.jolimark.JolimarkPrinterLanWriter
import com.baijunty.printer.jolimark.enums.ConnectTypeEnum
import com.baijunty.printer.jolimark.enums.PrinterEnum
import com.baijunty.printer.net.HttpPrinter
import com.baijunty.printer.net.LanPrinter
import java.nio.charset.Charset

/**
 *用于定义要打印的格式内容
 */

@Suppress("UNCHECKED_CAST")
sealed class PrintTaskBuilder {
    val rows = mutableListOf<Row>()
    /**
    * @param
    * @return 当前总行数
    */
    val lines get() = rows.size

    /**
     *占用一整行
    * @param value 行内容
    * @return  自身
    */
    open fun line(value: String): PrintTaskBuilder {
        val row = Row()
        row.columns.add(TextCell(value) as Cell<Any>)
        rows.add(row)
        return this
    }

    /**
     *占用一整行打印[value]并设置加粗[bold]，加高[heighten]，下划线[underLine]，对齐[align]
     */
    open fun line(value: String, bold: Boolean = false, heighten: Boolean = false,underLine:Boolean=false, align: Align = Align.LEFT): PrintTaskBuilder {
        val row = Row()
        row.columns.add(TextCell(value, Style(bold, double = heighten,underLine = underLine), align) as Cell<Any>)
        rows.add(row)
        return this
    }

    /**
     *生成[value]内容条形码打印
     * 占用整行
     */
    open fun barCode(value: String,type:BarCodeType=BarCodeType.Code93,width: Int=-1,height: Int=-1): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell(value, type = BarCode(type),width = width,height = height) as Cell<Any>)
        rows.add(row)
        return this
    }

    /**
     *生成[value]内容二维码打印
     * 占用整行
     */
    open fun qrCode(value: String,width:Int=-1,height:Int=-1): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell(value, type = QRCode,width = width,height = height) as Cell<Any>)
        rows.add(row)
        return this
    }
    /**
     *使用[supply]字节数组生成图片打印
     * 占用整行
     */
    open fun bitmap(supply: Supply<ByteArray, ImageCell>,width: Int,height: Int): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell("", type = Image, supply = supply,width = width,height = height) as Cell<Any>)
        rows.add(row)
        return this
    }

    abstract fun newLine(limit:Boolean=false,anchor: Int=-1): RowBuilder

    /**
     * 生成打印任为管理
     */
    abstract fun build(writer: PrinterWriter?=null): PrintWorkModel
}

/**
 *蓝牙打印机格式生成器
 * @param address 目标打印机地址
 * @property charset 目标打印机编码格式
 * @property printerType 打印机类型 默认为58打印机
 */
@Suppress("UNCHECKED_CAST")
 open class BlueToothPrinterTaskBuilder(private var address: String): PrintTaskBuilder() {
    var charset: Charset = Charset.forName("GBK")
    var printerType: BlueToothPrinter.Type = BlueToothPrinter.Type.Type58
    var printTime:Int=1
    var printer:PrintWorkModel?=null
    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String): BlueToothPrinterTaskBuilder {
        super.line(value)
        return this
    }

    fun setPrinterType(type:BlueToothPrinter.Type): BlueToothPrinterTaskBuilder {
        printerType=type
        return this
    }

    fun setPrintTime(time:Int):BlueToothPrinterTaskBuilder {
        printTime=time
        return this
    }
    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String, bold: Boolean, heighten: Boolean, underLine: Boolean, align: Align): BlueToothPrinterTaskBuilder {
        super.line(value, bold, heighten, underLine, align)
        return this
    }

    override fun barCode(
        value: String,
        type: BarCodeType,
        width: Int,
        height: Int
    ): BlueToothPrinterTaskBuilder {
         super.barCode(value, type, width, height)
        return this
    }
    /**
     *@see [PrintTaskBuilder.qrCode]
     */
    override fun qrCode(value: String,width: Int,height: Int): BlueToothPrinterTaskBuilder {
         super.qrCode(value,width, height)
        return this
    }

    override fun bitmap(
        supply: Supply<ByteArray, ImageCell>,
        width: Int,
        height: Int
    ): BlueToothPrinterTaskBuilder {
         super.bitmap(supply, width, height)
        return this
    }
    /**
     *行内容生成，支持多列
     * @param limit 是否列宽严格受限
     */
    override fun newLine(limit:Boolean,anchor:Int): BlueToothRowBuilder {
        val row = Row(rangeLimit = limit,anchor = anchor)
        rows.add(row)
        return BlueToothRowBuilder(row, this)
    }

    /**
     *使用行内容[func]列生成，支持多列
     * @param limit 是否列宽严格受限
     */
    fun newLine(limit:Boolean=false,anchor: Int=-1,func: BlueToothRowBuilder.() -> Unit): BlueToothPrinterTaskBuilder {
        val row = Row(rangeLimit = limit,anchor = anchor)
        rows.add(row)
        val builder = BlueToothRowBuilder(row, this)
        builder.func()
        builder.finish()
        return this
    }

    /**
     *使用字符[code]生成行分割线
     */
    fun divider(code: Char): BlueToothPrinterTaskBuilder {
        val row = Row()
        row.columns.add(TextCell(code.toString(), align = Align.FILL) as Cell<Any>)
        rows.add(row)
        return this
    }

    /**
     *前进[num]行
     */
    fun forward(num: Int): BlueToothPrinterTaskBuilder {
         for (i in 0 until num) {
             divider(' ')
         }
         return this
     }


    fun command(supply: Supply<ByteArray, CommandCell>, outData:ByteArray = ByteArray(0)):BlueToothPrinterTaskBuilder{
        val row = Row()
        row.columns.add(CommandCell(supply,outData))
        rows.add(row)
        return this
    }
    /**
     * 生成蓝牙打印任务
     */
    override fun  build(writer: PrinterWriter?): PrintWorkModel {
        return printer?:BlueToothPrinter.BLUETOOTH_PRINTER.apply {
            printTime= this@BlueToothPrinterTaskBuilder.printTime
            address=this@BlueToothPrinterTaskBuilder.address
            this.printerWriter= writer?:CommonBluetoothWriter(printerType,charset,rows)
        }
    }
}

class JolimarkPrinterTaskBuilder(val address: String):BlueToothPrinterTaskBuilder(address){
    private var printerEnum:PrinterEnum = PrinterEnum.CLP180
    private var connectType:ConnectTypeEnum =ConnectTypeEnum.BLUETOOTH
    private var isEsc =true
    fun setPrinter(printerEnum: PrinterEnum):JolimarkPrinterTaskBuilder{
        this.printerEnum=printerEnum
        return this
    }

    fun setConnectType(connectTypeEnum: ConnectTypeEnum):JolimarkPrinterTaskBuilder{
        connectType=connectTypeEnum
        return this
    }

    fun useHtml():JolimarkPrinterTaskBuilder{
        isEsc=false
        return this
    }

    override fun build(writer: PrinterWriter?): PrintWorkModel {
        return when (connectType) {
            ConnectTypeEnum.BLUETOOTH -> BlueToothPrinter.BLUETOOTH_PRINTER.apply {
                printTime = this@JolimarkPrinterTaskBuilder.printTime
                address = this@JolimarkPrinterTaskBuilder.address
                this.printerWriter =
                    writer ?: JolimarkBluetoothPrinterWriter(printerType, charset, rows, isEsc)
            }
            ConnectTypeEnum.LAN -> LanPrinter(
                writer ?: if (isEsc) CommonBluetoothWriter(printerType, charset, rows)
                else
                    JolimarkPrinterLanWriter(printerType, charset, rows),
                if (isEsc) 19100 else 10001
            ).apply {
                printTime = this@JolimarkPrinterTaskBuilder.printTime
                address = this@JolimarkPrinterTaskBuilder.address
            }
            ConnectTypeEnum.WLAN -> printer?:HttpPrinter(writer?:JolimarkHttpJsonWriter(rows))
        }
    }
}

@Suppress("UNCHECKED_CAST")
@TargetApi(Build.VERSION_CODES.KITKAT)
class HtmlPrinterTaskBuilder: PrintTaskBuilder(){
    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String): HtmlPrinterTaskBuilder {
        super.line(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String, bold: Boolean, heighten: Boolean, underLine: Boolean, align: Align): HtmlPrinterTaskBuilder {
        super.line(value, bold, heighten, underLine, align)
        return this
    }

    override fun barCode(
        value: String,
        type: BarCodeType,
        width: Int,
        height: Int
    ): HtmlPrinterTaskBuilder {
         super.barCode(value, type, width, height)
        return this
    }
    /**
     *@see [PrintTaskBuilder.qrCode]
     */
    override fun qrCode(value: String,width: Int,height: Int): HtmlPrinterTaskBuilder {
        super.qrCode(value,width, height)
        return this
    }

    override fun bitmap(
        supply: Supply<ByteArray, ImageCell>,
        width: Int,
        height: Int
    ): HtmlPrinterTaskBuilder {
         super.bitmap(supply, width, height)
        return this
    }
    /**
     *行内容生成器，[limit]控制列宽严格受限
     */
    override fun newLine(limit:Boolean,anchor: Int): HtmlRowBuilder {
        val row = Row(rangeLimit = limit,anchor = anchor)
        rows.add(row)
        return HtmlRowBuilder(row, this)
    }
    /**
     *[func]生成行内容，[limit]控制列宽严格受限
     */
    fun newLine(limit:Boolean=false,func: HtmlRowBuilder.() -> Unit): HtmlPrinterTaskBuilder {
        val row = Row(rangeLimit = limit)
        rows.add(row)
        val builder = HtmlRowBuilder(row, this)
        builder.func()
        builder.finish()
        return this
    }
    /**
     * 生成局域网打印任务
     */
    override fun build(writer: PrinterWriter?): PrintWorkModel {
        return HtmlPrinter(writer?:HtmlWriter(rows))
    }
}

@Suppress("UNCHECKED_CAST")
sealed class RowBuilder(protected val row: Row, protected val builder: PrintTaskBuilder) {

    /**
    * @param
    * @return 当前总列数
    */
    val size=row.columns.size

    /**
    * @param value 列文本内容
    * @return
    */
    fun string(value: String): RowBuilder {
        row.columns.add(TextCell(value) as Cell<Any>)
        return this
    }
    /**
     * @param value 列文本内容 设置加粗[bold]，加高[heighten]，下划线[underLine]，对齐[align] 以及列权重[weight]
     * @return
     */
    fun string(value: String, bold: Boolean = false, heighten: Boolean = false, underLine:Boolean=false, align: Align = Align.LEFT, weight: Int = 1): RowBuilder {
        row.columns.add(TextCell(value, Style(bold, double = heighten,underLine = underLine), align, weight = weight) as Cell<Any>)
        return this
    }
    /**
     *  [supply] 自定义列文本内容 设置加粗[bold]，加高[heighten]，下划线[underLine]，对齐[align] 以及列权重[weight]
     * @return
     */
    fun custom(supply: Supply<String, TextCell>, bold: Boolean = false, heighten: Boolean = false, underLine:Boolean=false, align: Align = Align.LEFT, weight: Int = 1): RowBuilder {
        row.columns.add(TextCell("", supply = supply) as Cell<Any>)
        return this
    }

    fun finish(): PrintTaskBuilder = builder
}

@Suppress("UNCHECKED_CAST")
class BlueToothRowBuilder(row: Row, val blueTaskBuilder: BlueToothPrinterTaskBuilder): RowBuilder(row,blueTaskBuilder) {
    /**
     * 填满整列
     */
    fun fill(value: String): BlueToothRowBuilder {
        row.columns.add(TextCell(value, align = Align.FILL) as Cell<Any>)
        return this
    }


    /**
     * 使用[supply]返回字节生成图片打印
     */
    fun bitmap(supply: Supply<ByteArray, ImageCell>): BlueToothPrinterTaskBuilder {
        row.columns.add(ImageCell("", type = Image, supply = supply,width = -1,height = -1) as Cell<Any>)
        return blueTaskBuilder
    }

    /**
     * 使用[value]生成条形码打印
     */
    fun barCode(value: String, type: BarCodeType=BarCodeType.Code93, width: Int, height: Int): BlueToothPrinterTaskBuilder {
        row.columns.add(ImageCell(value, type = BarCode(type),width = width,height = height) as Cell<Any>)
        return blueTaskBuilder
    }
    /**
     * 使用[value]生成二维码打印
     */
    fun qrCode(value: String,width: Int=-1,height: Int=-1): BlueToothPrinterTaskBuilder {
        row.columns.add(ImageCell(value, type = QRCode,width = width,height = height) as Cell<Any>)
        return blueTaskBuilder
    }
}

@Suppress("UNCHECKED_CAST")
 class HtmlRowBuilder(row: Row, val htmlTaskBuilder: HtmlPrinterTaskBuilder): RowBuilder(row,htmlTaskBuilder) {

    /**
     * 使用[supply]返回字节生成图片打印
     */
    fun bitmap(supply: Supply<ByteArray, ImageCell>,width: Int,height: Int): HtmlRowBuilder {
        row.columns.add(ImageCell("", type = Image, supply = supply,width = width,height = height) as Cell<Any>)
        return this
    }

    /**
     * 使用[value]生成条形码打印
     */
    fun barCode(value: String,type: BarCodeType=BarCodeType.Code93,width: Int,height: Int): HtmlRowBuilder {
        row.columns.add(ImageCell(value, type = BarCode(type),width = width,height = height) as Cell<Any>)
        return this
    }
    /**
     * 使用[value]生成二维码打印
     */
    fun qrCode(value: String,width: Int=-1,height: Int=-1): HtmlRowBuilder {
        row.columns.add(ImageCell(value, type = QRCode,width = width,height = height) as Cell<Any>)
        return this
    }
}