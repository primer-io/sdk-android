package io.primer.android.core.logging.serialization.json.extensions

import io.primer.android.core.logging.internal.WhitelistedKey
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.update(
    whitelistedKeys: List<WhitelistedKey>,
    newValue: String,
) {
    val keysToObfuscate = mutableListOf<String>()

    val primitiveKeys by lazy {
        whitelistedKeys.filterIsInstance<WhitelistedKey.PrimitiveWhitelistedKey>().map { it.value }.toSet()
    }

    for (currentKey in keys()) {
        val element = get(currentKey)
        when {
            element is JSONArray -> {
                element.update(whitelistedKeys.mapToChildren(currentKey), newValue)
            }

            element is JSONObject -> {
                element.update(whitelistedKeys.mapToChildren(currentKey), newValue)
            }

            currentKey !in primitiveKeys &&
                !isNull(currentKey) -> {
                keysToObfuscate += currentKey
            }
        }
    }

    for (key in keysToObfuscate) {
        put(key, newValue)
    }
}

private fun List<WhitelistedKey>.mapToChildren(key: String): List<WhitelistedKey> =
    filterIsInstance<WhitelistedKey.NonPrimitiveWhitelistedKey>().firstOrNull { it.value == key }
        ?.children.orEmpty()
