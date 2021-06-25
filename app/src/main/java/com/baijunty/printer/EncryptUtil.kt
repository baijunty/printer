package com.baijunty.printer

import java.security.MessageDigest

object EncryptUtil {
    fun String.md5(uppercase:Boolean=false):String{
        val md = MessageDigest.getInstance("MD5")
        md.update(this.toByteArray())
        val b = md.digest()
        val buf = StringBuilder()
        for (k in b) {
            val i = k.toInt() and 0xff
            if (i < 16) buf.append("0")
            buf.append(Integer.toHexString(i))
        }
        return buf.toString().let {
            if (uppercase) it.uppercase() else it
        }
    }
}