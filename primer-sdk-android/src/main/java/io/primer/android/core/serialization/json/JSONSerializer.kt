package io.primer.android.core.serialization.json

import org.json.JSONObject

/**
 * An interface describing simple JSON serializer.
 * @param T an object to be serialized.
 * @return JSON representation of given object.
 */

internal interface JSONSerializer<T : JSONSerializable> {
    fun serialize(t: T): JSONObject
}
