package io.primer.android.core.data.serialization.json.extensions

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MapTest {
    @Test
    fun `toJSONObject converts nested maps to JSONObjects recursively`() {
        val map =
            mapOf(
                "outer" to
                    mapOf(
                        "inner" to mapOf("value" to 42),
                    ),
            )

        val json = map.toJSONObject()

        assertEquals(
            42,
            json.getJSONObject("outer").getJSONObject("inner").getInt("value"),
        )
    }

    @Test
    fun `toJSONObject converts nested collections to JSONArrays recursively`() {
        val map =
            mapOf(
                "list" to listOf("plain", listOf(1, 2), mapOf("key" to "value")),
            )

        val json = map.toJSONObject()

        val array = json.getJSONArray("list")
        assertEquals(3, array.length())
        assertEquals("plain", array.getString(0))
        assertEquals(2, array.getJSONArray(1).getInt(1))
        assertEquals("value", array.getJSONObject(2).getString("key"))
    }

    @Test
    fun `toJSONObject converts null values to JSONObject NULL`() {
        val json = mapOf<String, Any?>("nullValue" to null).toJSONObject()

        assertEquals(true, json.has("nullValue"))
        assertEquals(JSONObject.NULL, json.get("nullValue"))
    }

    @Test
    fun `toJSONObject round-trips through toMap with nested maps lists and nulls`() {
        val original =
            mapOf(
                "string" to "value",
                "number" to 42,
                "nullValue" to null,
                "nested" to
                    mapOf(
                        "inner" to "x",
                        "innerList" to listOf(1, 2),
                    ),
                "list" to listOf(mapOf("a" to "b"), "plain", null),
            )

        assertEquals(original, original.toJSONObject().toMap())
    }
}
