package io.primer.android.core.serialization.json.extensions.obfuscation

import io.primer.android.core.logging.internal.WhitelistedKey
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONArray.update(whitelistedKeys: List<WhitelistedKey>, newValue: String) {
    for (index in 0 until length()) {
        val element = get(index)
        if (element is JSONArray) {
            element.update(whitelistedKeys, newValue)
        } else if (element is JSONObject) {
            element.update(whitelistedKeys, newValue)
        }
    }
}
