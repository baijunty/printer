### Android客户端打印内容格式设计生成器
- 支持蓝牙打印和普通打印机。
- 每行可自定义列内容，设置列宽度权重，加粗加高等
- 支持图片，条码，二维码
- 打印前支持预览
```Kotlin
        val printer = BlueToothPrinterTaskBuilder(address,activity!!).apply{
            //打印机类型
            printerType=BlueToothPrinter.Type.Type80
        }
        .line("张山小卖铺", bold = true, heighten = true, underLine=true,align=Align.CENTER)
        .line("单据日期:2019年2月13日")
        .line("单据编号:2019年2月13日")
        .divider('-')
        .newLine {
			string("名称",weight = 3).string("单位", align = Align.CENTER).string("数量", align =Align.RIGHT).string("金额",
			align=Align.RIGHT)
		}
		.divider('-')
		.newLine(true) {
			string("南山超市最新最大的大洋芋",weight = 3).string("个", align = Align.CENTER).
			string("1.00", align = Align.RIGHT).string("3.45", align = Align.RIGHT)
        }
		.newLine(true) {
			string("中关村电影").string("个", align = Align.CENTER).
			string("1.00", align = Align.RIGHT)
		 }
		 .divider('-')
		 .line("总数量:1.00").line("总金额:5.415")
		 .forward(1)
		 .qrCode("www.baidu.com").barCode("123")
		 .build()
		 //预览打印内容和效果
		 //val builder= AlertDialog.Builder(activity!!)
		 //builder.setView(printer.preview())
		 //builder.show()
		 //打印 
		 printer.print(object : PrinterListener {
		 override fun onFinish(success: Boolean, error: Throwable?) {
		 	if (success) {
				it.showSnackbar("打印成功")
			} else {
				it.showSnackbar("打印失败{error.message}")
			}
		}
	})
```
[![](https://jitpack.io/v/baijunty/printer.svg)](https://jitpack.io/#baijunty/printer)
