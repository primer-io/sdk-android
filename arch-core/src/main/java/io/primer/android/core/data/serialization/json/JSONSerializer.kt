package io.primer.android.core.data.serialization.json

import org.json.JSONArray
import org.json.JSONObject

/**
 * An interface describing base interface for [JSONObject] and [JSONArray] serializers.
 * @param T an object to be serialized.
 */
sealed interface JSONSerializer<T : JSONSerializable>
