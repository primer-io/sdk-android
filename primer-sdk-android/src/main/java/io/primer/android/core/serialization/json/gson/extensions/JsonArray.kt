package io.primer.android.core.serialization.json.gson.extensions

import com.google.gson.JsonArray
import io.primer.android.core.logging.internal.WhitelistedKey

internal fun JsonArray.update(whitelistedKeys: List<WhitelistedKey>, newValue: String) {
    for (element in this) {
        if (element.isJsonArray) {
            element.asJsonArray.update(whitelistedKeys, newValue)
        } else if (element.isJsonObject) {
            element.asJsonObject.update(whitelistedKeys, newValue)
        }
    }
}
