package com.yxqyrh.janusandroid.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object MoshiUtil {
    val moshiBuilder: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    inline fun <reified T> toJson(t: T): String =
        moshiBuilder.adapter(T::class.java).toJson(t)

    inline fun <reified T> fromJson(string: String) =
        moshiBuilder.adapter(T::class.java).fromJson(string)

    inline fun <reified T> toJson(t: T, type: Type): String =
        moshiBuilder.adapter<T>(type).toJson(t)

    inline fun <reified T> fromJson(string: String, type: Type) =
        moshiBuilder.adapter<T>(type).fromJson(string)

    fun generateListType(type: Type): ParameterizedType =
        Types.newParameterizedType(List::class.java, type)
}