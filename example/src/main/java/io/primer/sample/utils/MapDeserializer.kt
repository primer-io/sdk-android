package io.primer.sample.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

object MapDeserializer : JsonDeserializer<Map<String, Any>> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<String, Any> =
        json.asJsonObject.entrySet().associate { (key, value) ->
            key to when {
                value.isJsonPrimitive -> {
                    when {
                        value.asJsonPrimitive.isString -> value.asString
                        value.asJsonPrimitive.isNumber -> value.asNumber
                        value.asJsonPrimitive.isBoolean -> value.asBoolean
                        else -> error("Unsupported type")
                    }
                }

                value.isJsonObject -> context.deserialize<Map<String, Any>>(value, Map::class.java)
                value.isJsonArray -> context.deserialize<List<Any>>(value, List::class.java)
                else -> error("Unsupported type")
            }
        }
}
