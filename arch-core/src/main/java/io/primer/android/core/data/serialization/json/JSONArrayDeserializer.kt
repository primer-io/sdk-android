package io.primer.android.core.data.serialization.json

import org.json.JSONArray

/**
 * An interface describing simple JSON deserializer.
 * @param [JSONArray] an array to be deserialized.
 * @return Deserialized object T.
 */

fun interface JSONArrayDeserializer<T : JSONDeserializable> : JSONDeserializer<T> {
    fun deserialize(t: JSONArray): T
}
