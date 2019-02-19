package com.uplus.printer

import android.arch.lifecycle.LifecycleObserver
import android.content.Context
import android.view.View
import java.io.Closeable

/**
 * Created by bai on 2018-02-08.
 * 打印任务
 * @property writer 用于生成打印内容
 */
interface PrintWorkModel : Closeable {
    var writer:PrinterWriter
    fun print(context: Context,listener:PrinterListener)
    fun preview(context: Context):View
    fun cancel()
}

/**
 * 打印任为监听回调
 * [onSuccess]
 */
interface PrinterListener{
    /**
     * [success]是否打印成功,[error]打印过程异常，包括格式不正确，打印机连接调用异常
     */
    fun onFinish(success:Boolean,error:Throwable?)
}