package com.baijunty.printer

import okhttp3.internal.and
import java.security.MessageDigest

object EncryptUtil {
    fun String.md5():String{
        val md = MessageDigest.getInstance("MD5")
        md.update(this.toByteArray())
        val b = md.digest()
        val buf = StringBuilder()
        for (k in b) {
            val i = k and 0xff
            if (i < 16) buf.append("0")
            buf.append(Integer.toHexString(i))
        }
        return buf.toString()
    }
}