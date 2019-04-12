package com.uplus.printer

import android.annotation.TargetApi
import android.os.Build
import com.uplus.printer.bluetooth.BlueToothPrinter
import com.uplus.printer.bluetooth.CommonBluetoothWriter
import com.uplus.printer.html.HtmlPrinter
import com.uplus.printer.html.HtmlWriter
import java.nio.charset.Charset

/**
 *用于定义要打印的格式内容
 */

@Suppress("UNCHECKED_CAST")
sealed class PrintTaskBuilder {
    protected val rows = mutableListOf<Row>()
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
    open fun barCode(value: String): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell(value, type = ImageType.BARCODE) as Cell<Any>)
        rows.add(row)
        return this
    }

    /**
     *生成[value]内容二维码打印
     * 占用整行
     */
    open fun qrCode(value: String): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell(value, type = ImageType.QR_CODE) as Cell<Any>)
        rows.add(row)
        return this
    }
    /**
     *使用[supply]字节数组生成图片打印
     * 占用整行
     */
    open fun bitmap(supply: Supply<ByteArray, ImageCell>): PrintTaskBuilder {
        val row = Row()
        row.columns.add(ImageCell("", type = ImageType.IMAGE, supply = supply) as Cell<Any>)
        rows.add(row)
        return this
    }

    abstract fun newLine(limit:Boolean=false): RowBuilder

    /**
     * 生成打印任为管理
     */
    abstract fun build(): PrintWorkModel
}

/**
 *蓝牙打印机格式生成器
 * @param address 目标打印机地址
 * @param activity 用于管理生命周期
 * @property charset 目标打印机编码格式
 * @property printerType 打印机类型 默认为58打印机
 */
@Suppress("UNCHECKED_CAST")
 class BlueToothPrinterTaskBuilder(private var address: String):PrintTaskBuilder() {
    var charset: Charset = Charset.forName("GBK")
    var printerType: BlueToothPrinter.Type = BlueToothPrinter.Type.Type58

    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String): BlueToothPrinterTaskBuilder {
        super.line(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.line]
     */
    override fun line(value: String, bold: Boolean, heighten: Boolean, underLine: Boolean, align: Align): BlueToothPrinterTaskBuilder {
        super.line(value, bold, heighten, underLine, align)
        return this
    }
    /**
     *@see [PrintTaskBuilder.barCode]
     */
    override fun barCode(value: String): BlueToothPrinterTaskBuilder {
        super.barCode(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.qrCode]
     */
    override fun qrCode(value: String): BlueToothPrinterTaskBuilder {
         super.qrCode(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.bitmap]
     */
    override fun bitmap(supply: Supply<ByteArray, ImageCell>): BlueToothPrinterTaskBuilder {
        super.bitmap(supply)
        return this
    }
    /**
     *行内容生成，支持多列
     * @param limit 是否列宽严格受限
     */
    override fun newLine(limit:Boolean): BlueToothRowBuilder {
        val row = Row(rangeLimit = limit)
        rows.add(row)
        val builder = BlueToothRowBuilder(row, this)
        return builder
    }

    /**
     *使用行内容[func]列生成，支持多列
     * @param limit 是否列宽严格受限
     */
    fun newLine(limit:Boolean=false,func: BlueToothRowBuilder.() -> Unit): BlueToothPrinterTaskBuilder {
        val row = Row(rangeLimit = limit)
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

    /**
     * 生成蓝牙打印任务
     */
    override fun build(): PrintWorkModel {
        return BlueToothPrinter.BLUETOOTH_PRINTER.apply {
            this.address=address
            writer=CommonBluetoothWriter(printerType,charset,rows)
        }
    }
}

@Suppress("UNCHECKED_CAST")
@TargetApi(Build.VERSION_CODES.KITKAT)
class HtmlPrinterTaskBuilder:PrintTaskBuilder(){
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
    /**
     *@see [PrintTaskBuilder.barCode]
     */
    override fun barCode(value: String): HtmlPrinterTaskBuilder {
        super.barCode(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.qrCode]
     */
    override fun qrCode(value: String): HtmlPrinterTaskBuilder {
        super.qrCode(value)
        return this
    }
    /**
     *@see [PrintTaskBuilder.bitmap]
     */
    override fun bitmap(supply: Supply<ByteArray, ImageCell>): HtmlPrinterTaskBuilder {
        super.bitmap(supply)
        return this
    }
    /**
     *行内容生成器，[limit]控制列宽严格受限
     */
    override fun newLine(limit:Boolean): HtmlRowBuilder {
        val row = Row(rangeLimit = limit)
        rows.add(row)
        val builder = HtmlRowBuilder(row, this)
        return builder
    }
    /**
     *[func]生成行内容，[limit]控制列宽严格受限
     */
    fun newLine(limit:Boolean=false,func: HtmlRowBuilder.() -> Unit): HtmlPrinterTaskBuilder {
        val row = Row(rangeLimit = limit)
        rows.add(row)
        val builder =HtmlRowBuilder(row, this)
        builder.func()
        builder.finish()
        return this
    }
    /**
     * 生成局域网打印任务
     */
    override fun build(): PrintWorkModel {
        return HtmlPrinter(HtmlWriter(rows))
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
    fun string(value: String, bold: Boolean = false, heighten: Boolean = false, underLine:Boolean=false,align: Align = Align.LEFT, weight: Int = 1): RowBuilder {
        row.columns.add(TextCell(value, Style(bold, double = heighten,underLine = underLine), align, weight = weight) as Cell<Any>)
        return this
    }
    /**
     *  [supply] 自定义列文本内容 设置加粗[bold]，加高[heighten]，下划线[underLine]，对齐[align] 以及列权重[weight]
     * @return
     */
    fun custom(supply: Supply<String, TextCell>,bold: Boolean = false, heighten: Boolean = false, underLine:Boolean=false,align: Align = Align.LEFT, weight: Int = 1): RowBuilder {
        row.columns.add(TextCell("", supply = supply) as Cell<Any>)
        return this
    }


    fun finish(): PrintTaskBuilder = builder
}

@Suppress("UNCHECKED_CAST")
class BlueToothRowBuilder( row: Row,  val blueTaskBuilder: BlueToothPrinterTaskBuilder):RowBuilder(row,blueTaskBuilder) {
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
        row.columns.add(ImageCell("", type = ImageType.IMAGE, supply = supply) as Cell<Any>)
        return blueTaskBuilder
    }

    /**
     * 使用[value]生成条形码打印
     */
    fun barCode(value: String): BlueToothPrinterTaskBuilder {
        row.columns.add(ImageCell(value, type = ImageType.BARCODE) as Cell<Any>)
        return blueTaskBuilder
    }
    /**
     * 使用[value]生成二维码打印
     */
    fun qrCode(value: String): BlueToothPrinterTaskBuilder {
        row.columns.add(ImageCell(value, type = ImageType.QR_CODE) as Cell<Any>)
        return blueTaskBuilder
    }
}

@Suppress("UNCHECKED_CAST")
 class HtmlRowBuilder( row: Row,val htmlTaskBuilder: HtmlPrinterTaskBuilder):RowBuilder(row,htmlTaskBuilder) {

    /**
     * 使用[supply]返回字节生成图片打印
     */
    fun bitmap(supply: Supply<ByteArray, ImageCell>): HtmlRowBuilder {
        row.columns.add(ImageCell("", type = ImageType.IMAGE, supply = supply) as Cell<Any>)
        return this
    }

    /**
     * 使用[value]生成条形码打印
     */
    fun barCode(value: String): HtmlRowBuilder {
        row.columns.add(ImageCell(value, type = ImageType.BARCODE) as Cell<Any>)
        return this
    }
    /**
     * 使用[value]生成二维码打印
     */
    fun qrCode(value: String): HtmlRowBuilder {
        row.columns.add(ImageCell(value, type = ImageType.QR_CODE) as Cell<Any>)
        return this
    }
}