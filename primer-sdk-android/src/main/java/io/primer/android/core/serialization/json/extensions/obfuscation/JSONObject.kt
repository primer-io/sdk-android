package io.primer.android.core.serialization.json.extensions.obfuscation

import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.WhitelistedKey.NonPrimitiveWhitelistedKey
import io.primer.android.core.logging.internal.WhitelistedKey.PrimitiveWhitelistedKey
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.update(whitelistedKeys: List<WhitelistedKey>, newValue: String) {
    val keysToObfuscate = mutableListOf<String>()

    val primitiveKeys by lazy {
        whitelistedKeys.filterIsInstance<PrimitiveWhitelistedKey>().map { it.value }.toSet()
    }

    for (currentKey in keys()) {
        val element = get(currentKey)
        if (element is JSONArray) {
            element.update(whitelistedKeys.mapToChildren(currentKey), newValue)
        } else if (element is JSONObject) {
            element.update(whitelistedKeys.mapToChildren(currentKey), newValue)
        } else if (
            currentKey !in primitiveKeys &&
            !isNull(currentKey)
        ) {
            keysToObfuscate += currentKey
        }
    }

    for (key in keysToObfuscate) {
        put(key, newValue)
    }
}

private fun List<WhitelistedKey>.mapToChildren(key: String): List<WhitelistedKey> =
    filterIsInstance<NonPrimitiveWhitelistedKey>().firstOrNull { it.value == key }
        ?.children.orEmpty()
