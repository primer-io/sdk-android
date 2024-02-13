package io.primer.android.core.serialization.json

import org.json.JSONArray
import org.json.JSONObject

internal object JSONDataUtils {

    fun stringToJsonData(jsonString: String): JSONData {
        return if (runCatching { JSONObject(jsonString) }.getOrNull() != null) {
            JSONData.JSONObjectData(JSONObject(jsonString))
        } else {
            JSONData.JSONArrayData(JSONArray(jsonString))
        }
    }

    internal sealed class JSONData {
        data class JSONObjectData(val json: JSONObject) : JSONData()
        data class JSONArrayData(val json: JSONArray) : JSONData()
    }
}
