package com.baijunty.printer

/**
 * 用于提供打印数据内容
 * [WORD]最小字节2
 * [DEFAULT_STYLE]默认不设置任何格式
 * [OriginSupply]使用提供名称作为内容打印
 *  [EmptyBytesSupply]默认空字节
 */
interface Supply<R,T: Cell<R>> {
    fun getValue(t:T):R
}
const val WORD=2

 val DEFAULT_STYLE= Style()

val OriginSupply: Supply<String, TextCell> = object : Supply<String, TextCell> {
    override fun getValue(t: TextCell): String =t.content
}

val EmptyBytesSupply: Supply<ByteArray, ImageCell> = object : Supply<ByteArray, ImageCell> {
    override fun getValue(t: ImageCell): ByteArray = byteArrayOf()
}

/**
 * 行定义 [gap]列间距,[rangeLimit]列宽是否严格受限,[anchor]参考行，用于蓝牙打印对齐
 * @property columns 每行的数据列
 */
class Row(val gap:Int= WORD, val rangeLimit:Boolean=false,val anchor:Int=-1) {
    val columns= mutableListOf<Cell<*>>()
}

/**
 * 数据列 [supply]提供列内容，[weight]定义权重
 */
sealed class Cell<T>(private val supply: Supply<T, Cell<T>>, val weight:Int=1){
    fun getValue():T=supply.getValue(this)
}

/**
 * 文本列 [content]打印,[style]文字格式,[align]对齐格式,[supply]默认使用[content]
 */
@Suppress("UNCHECKED_CAST")
class TextCell(val content:String, val style: Style = DEFAULT_STYLE, val align: Align = Align.LEFT, supply: Supply<String, TextCell> = OriginSupply, weight:Int=1):
        Cell<String>(supply as Supply<String, Cell<String>>,weight)
/**
 * 图像列 [content]打印,[type]图形格式,[supply]默认使用空
 */
@Suppress("UNCHECKED_CAST")
class ImageCell(val content:String, val type: ImageType,supply: Supply<ByteArray, ImageCell> = EmptyBytesSupply, weight:Int=1,
                val width:Int=-1,val height:Int=-1):
        Cell<ByteArray>(supply as Supply<ByteArray, Cell<ByteArray>>,weight)

/**
 * 额外原生指令
 */
@Suppress("UNCHECKED_CAST")
class CommandCell(supply: Supply<ByteArray, CommandCell>): Cell<ByteArray>(supply as Supply<ByteArray, Cell<ByteArray>>,1)

/**
 * 文本对齐
 */
enum class Align{
    LEFT,CENTER,RIGHT,FILL
}

/**
 * 文字设置
 * [bold]加粗 [double]加高，对HTML是双倍大小
 * [underLine]下划线
 */
data class Style(
    val bold:Boolean=false,
    val double:Boolean=false,
    val underLine:Boolean=false
)
/**
 * 图片格式
 */
enum class BarCodeType(val value:Int){
    Code128(73),Code93(72),UPCA(65),CODE39(69),CODABAR(71);

    override fun toString(): String{
        return when(this){
            Code128 -> "128"
            Code93 -> "93"
            UPCA -> "UPCA"
            CODE39 -> "39"
            CODABAR -> "CODA"
        }
    }

}
sealed class ImageType
class BarCode(val type: BarCodeType):ImageType()
object QRCode : ImageType()
object Image : ImageType()
