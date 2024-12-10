package io.primer.android.core.data.serialization.json

import org.json.JSONObject

/**
 * An interface describing simple JSON deserializer.
 * @param [JSONObject] an object to be deserialized.
 * @return Deserialized object T.
 */

fun interface JSONObjectDeserializer<T : JSONDeserializable> : JSONDeserializer<T> {
    fun deserialize(t: JSONObject): T
}
