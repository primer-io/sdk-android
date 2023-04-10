package io.primer.android.core.serialization.json

import org.json.JSONObject
import org.json.JSONArray

/**
 * An interface describing base interface for [JSONObject] and [JSONArray] serializers.
 * @param T an object to be serialized.
 */
internal sealed interface JSONSerializer<T : JSONSerializable>
