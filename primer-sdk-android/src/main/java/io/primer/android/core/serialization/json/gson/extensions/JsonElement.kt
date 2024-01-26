package io.primer.android.core.serialization.json.gson.extensions

import com.google.gson.JsonElement
import io.primer.android.core.logging.internal.WhitelistedKey

internal fun JsonElement.update(whitelistedKeys: List<WhitelistedKey>, newValue: String) {
    if (isJsonObject) {
        asJsonObject.update(
            whitelistedKeys = whitelistedKeys,
            newValue = newValue
        )
    } else if (isJsonArray) {
        asJsonArray.update(
            whitelistedKeys = whitelistedKeys,
            newValue = newValue
        )
    }
}
