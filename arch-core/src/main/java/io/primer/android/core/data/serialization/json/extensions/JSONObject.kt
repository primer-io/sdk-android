package io.primer.android.core.data.serialization.json.extensions

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.optNullableString(
    name: String,
    fallback: String? = null,
): String? {
    return if (has(name) && !isNull(name)) {
        this.getString(name)
    } else {
        fallback
    }
}

fun JSONObject.optNullableBoolean(
    name: String,
    fallback: Boolean? = null,
): Boolean? {
    return if (has(name) && !isNull(name)) {
        this.getBoolean(name)
    } else {
        fallback
    }
}

fun JSONObject.optNullableFloat(
    name: String,
    fallback: Float? = null,
): Float? {
    return if (has(name) && !isNull(name)) {
        getDouble(name).toFloat()
    } else {
        fallback
    }
}

fun JSONObject.optNullableInt(
    name: String,
    fallback: Int? = null,
): Int? {
    return if (has(name) && !isNull(name)) {
        getInt(name)
    } else {
        fallback
    }
}

fun JSONObject.optNullableLong(
    name: String,
    fallback: Long? = null,
): Long? {
    return if (has(name) && !isNull(name)) {
        getLong(name)
    } else {
        fallback
    }
}

fun JSONObject.optNullableObject(
    name: String,
    fallback: JSONObject? = null,
): JSONObject? {
    return if (has(name) && !isNull(name)) {
        getJSONObject(name)
    } else {
        fallback
    }
}

fun JSONObject.toBooleanMap(): Map<String, Boolean> {
    return keys().asSequence().associateWith {
        getBoolean(it)
    }
}

fun JSONObject.toStringMap(): Map<String, String> {
    return keys().asSequence().associateWith {
        getString(it)
    }
}

fun JSONObject.toMap(): Map<String, *> {
    return keys().asSequence().associateWith {
        when (val value = this[it]) {
            is JSONArray -> {
                val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                JSONObject(map).toMap().values.toList()
            }

            is JSONObject -> value.toMap()
            JSONObject.NULL -> null
            else -> value
        }
    }
}
