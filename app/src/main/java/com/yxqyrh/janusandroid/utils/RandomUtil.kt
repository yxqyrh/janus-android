package com.yxqyrh.janusandroid.utils

import java.util.*

object RandomUtil {

    private const val str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun randomTransaction(length: Int = 12) =
        with(StringBuilder(length)) {
            append(System.currentTimeMillis())
            append("_")
            val rand = Random()
            for (index in 0 until length) {
                append(str[rand.nextInt(str.length)])
            }
            toString()
        }
}