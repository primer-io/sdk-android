package io.primer.android.core.data.serialization.json.extensions

import org.json.JSONArray
import org.json.JSONObject

fun <T> JSONArray.sequence(): Sequence<T> = (0 until this.length()).asSequence().map { this[it] as T }

fun JSONArray.toList(): List<Any?> =
    List(length()) { index ->
        when (val value = opt(index)) {
            is JSONObject -> value.toMap()
            is JSONArray -> value.toList()
            JSONObject.NULL -> null
            else -> value
        }
    }
