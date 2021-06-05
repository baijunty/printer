package com.baijunty.printer

import android.content.Context
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.io.Closeable

/**
 * Created by bai on 2018-02-08.
 * 打印任务
 * @property writer 用于生成打印内容
 */
interface PrintWorkModel : Closeable {
    fun print(context: Context):Observable<Boolean>
    fun preview(context: Context):Observable<View>
}