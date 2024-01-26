package io.primer.android.core.serialization.json.gson.extensions

import com.google.gson.JsonObject
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.WhitelistedKey.NonPrimitiveWhitelistedKey
import io.primer.android.core.logging.internal.WhitelistedKey.PrimitiveWhitelistedKey

internal fun JsonObject.update(whitelistedKeys: List<WhitelistedKey>, newValue: String) {
    val keysToObfuscate = mutableListOf<String>()

    val primitiveKeys by lazy {
        whitelistedKeys.filterIsInstance<PrimitiveWhitelistedKey>().map { it.value }.toSet()
    }

    for ((currentKey, element) in entrySet()) {
        if (element.isJsonArray) {
            element.asJsonArray.update(whitelistedKeys.mapToChildren(currentKey), newValue)
        } else if (element.isJsonObject) {
            element.asJsonObject.update(whitelistedKeys.mapToChildren(currentKey), newValue)
        } else if (
            element.isJsonPrimitive &&
            currentKey !in primitiveKeys &&
            !element.asJsonPrimitive.isJsonNull
        ) {
            keysToObfuscate += currentKey
        }
    }

    for (key in keysToObfuscate) {
        addProperty(key, newValue)
    }
}

private fun List<WhitelistedKey>.mapToChildren(key: String): List<WhitelistedKey> =
    filterIsInstance<NonPrimitiveWhitelistedKey>().firstOrNull { it.value == key }
        ?.children.orEmpty()
