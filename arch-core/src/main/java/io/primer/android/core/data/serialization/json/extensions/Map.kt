package io.primer.android.core.data.serialization.json.extensions

import org.json.JSONArray
import org.json.JSONObject

/**
 * Recursively converts a map into a [JSONObject]: nested maps become [JSONObject]s, collections
 * become [JSONArray]s (recursing into elements) and `null` becomes [JSONObject.NULL]. The exact
 * inverse of [JSONObject.toMap]. The [JSONObject] `(Map)` constructor is deliberately avoided:
 * the Android runtime does not deep-wrap nested values like the JVM `org.json` does.
 */
fun Map<String, Any?>.toJSONObject(): JSONObject =
    entries.fold(JSONObject()) { json, (key, value) -> json.put(key, value.toJsonValue()) }

/** Recursively converts a collection into a [JSONArray]; see [toJSONObject]. */
fun Collection<Any?>.toJSONArray(): JSONArray =
    fold(JSONArray()) { array, value -> array.put(value.toJsonValue()) }

private fun Any?.toJsonValue(): Any =
    when (this) {
        null -> JSONObject.NULL
        is Map<*, *> ->
            entries.fold(JSONObject()) { json, (key, value) ->
                json.put(key.toString(), value.toJsonValue())
            }

        is Collection<*> -> toJSONArray()
        else -> this
    }
