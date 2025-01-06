package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RetailOutletResultDataResponseTest {
    @Test
    fun `test RetailOutletDataResponse deserialization`() {
        val jsonString = """
            {
                "id": "outlet_id_123",
                "name": "Test Outlet",
                "disabled": false,
                "iconUrl": "https://example.com/icon.png"
            }
        """
        val jsonObject = JSONObject(jsonString)
        val dataResponse = RetailOutletDataResponse.deserializer.deserialize(jsonObject)

        assertEquals("outlet_id_123", dataResponse.id)
        assertEquals("Test Outlet", dataResponse.name)
        assertEquals(false, dataResponse.disabled)
        assertEquals("https://example.com/icon.png", dataResponse.iconUrl)
    }

    @Test
    fun `test RetailOutletResultDataResponse deserialization`() {
        val jsonString = """
            {
                "result": [
                    {
                        "id": "outlet_id_123",
                        "name": "Test Outlet",
                        "disabled": false,
                        "iconUrl": "https://example.com/icon.png"
                    },
                    {
                        "id": "outlet_id_456",
                        "name": "Another Outlet",
                        "disabled": true,
                        "iconUrl": "https://example.com/icon2.png"
                    }
                ]
            }
        """
        val jsonObject = JSONObject(jsonString)
        val resultDataResponse = RetailOutletResultDataResponse.deserializer.deserialize(jsonObject)

        assertEquals(2, resultDataResponse.result.size)

        val firstOutlet = resultDataResponse.result[0]
        assertEquals("outlet_id_123", firstOutlet.id)
        assertEquals("Test Outlet", firstOutlet.name)
        assertEquals(false, firstOutlet.disabled)
        assertEquals("https://example.com/icon.png", firstOutlet.iconUrl)

        val secondOutlet = resultDataResponse.result[1]
        assertEquals("outlet_id_456", secondOutlet.id)
        assertEquals("Another Outlet", secondOutlet.name)
        assertEquals(true, secondOutlet.disabled)
        assertEquals("https://example.com/icon2.png", secondOutlet.iconUrl)
    }
}
