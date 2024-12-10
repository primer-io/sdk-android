package io.primer.android.core.data.serialization.json

import org.json.JSONArray

/**
 * An interface describing simple [JSONArray] serializer.
 * Usually, this would be a wrapper object containing a collection that must be represented as
 * [JSONArray] at top level. Otherwise, we can serialize every collection member, and put it to
 * a designated [JSONArray].
 * @param T an object to be serialized.
 * @return [JSONArray] representation of given object.
 */

interface JSONArraySerializer<T : JSONArraySerializable> : JSONSerializer<T> {
    fun serialize(t: T): JSONArray
}
