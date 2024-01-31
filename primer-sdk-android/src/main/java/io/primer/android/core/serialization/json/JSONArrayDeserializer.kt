package io.primer.android.core.serialization.json

import org.json.JSONArray

/**
 * An interface describing simple JSON deserializer.
 * @param [JSONArray] an array to be deserialized.
 * @return Deserialized object T.
 */

internal fun interface JSONArrayDeserializer<T : JSONDeserializable> : JSONDeserializer<T> {
    fun deserialize(t: JSONArray): T
}
