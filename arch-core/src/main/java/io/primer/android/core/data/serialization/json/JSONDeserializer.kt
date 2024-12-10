package io.primer.android.core.data.serialization.json

import org.json.JSONArray
import org.json.JSONObject

/**
 * An interface describing base interface for [JSONObject] and [JSONArray] deserialization.
 * @param T an object to be deserialized.
 */
sealed interface JSONDeserializer<T : JSONDeserializable>
