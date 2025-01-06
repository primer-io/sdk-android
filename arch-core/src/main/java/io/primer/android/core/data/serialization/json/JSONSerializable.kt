package io.primer.android.core.data.serialization.json

import org.json.JSONArray
import org.json.JSONObject

/**
Interface describing class that can be serialized to JSON.
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
interface JSONSerializable

/**
Interface describing class that can be serialized to [JSONObject].
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
interface JSONObjectSerializable : JSONSerializable

/**
Interface describing class that can be serialized to [JSONArray].
Every class implementing this interface should define @JvmField field 'serializer' in the companion object.
 */
interface JSONArraySerializable : JSONSerializable
