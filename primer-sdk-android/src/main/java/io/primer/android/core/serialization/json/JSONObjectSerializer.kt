package io.primer.android.core.serialization.json

import org.json.JSONObject

/**
 * An interface describing simple [JSONObject] serializer.
 * @param T an object to be serialized.
 * @return [JSONObject] representation of given object.
 */
internal fun interface JSONObjectSerializer<T : JSONObjectSerializable> : JSONSerializer<T> {
    fun serialize(t: T): JSONObject
}
