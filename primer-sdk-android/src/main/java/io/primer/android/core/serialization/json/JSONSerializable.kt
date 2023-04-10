package io.primer.android.core.serialization.json

import org.json.JSONObject
import org.json.JSONArray

/**
Interface describing class that can be serialized to JSON.
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
internal interface JSONSerializable

/**
Interface describing class that can be serialized to [JSONObject].
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
internal interface JSONObjectSerializable : JSONSerializable

/**
Interface describing class that can be serialized to [JSONArray].
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
internal interface JSONArraySerializable : JSONSerializable
