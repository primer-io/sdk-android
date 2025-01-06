package io.primer.android.banks.implementation.rpc.data.models

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IssuingBankResultDataResponseTest {
    @Test
    fun `test deserializer deserializes correctly`() {
        // Arrange
        val jsonArray =
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        put(
                            IssuingBankDataResponse.ID_FIELD,
                            "bank_id_123",
                        )
                        put(
                            IssuingBankDataResponse.NAME_FIELD,
                            "Bank ABC",
                        )
                        put(
                            IssuingBankDataResponse.DISABLED_FIELD,
                            false,
                        )
                        put(
                            IssuingBankDataResponse.ICON_URL_FIELD,
                            "http://icon.url",
                        )
                    },
                )
                put(
                    JSONObject().apply {
                        put(
                            IssuingBankDataResponse.ID_FIELD,
                            "bank_id_456",
                        )
                        put(
                            IssuingBankDataResponse.NAME_FIELD,
                            "Bank XYZ",
                        )
                        put(
                            IssuingBankDataResponse.DISABLED_FIELD,
                            true,
                        )
                        put(
                            IssuingBankDataResponse.ICON_URL_FIELD,
                            "http://icon.xyz",
                        )
                    },
                )
            }

        val json =
            JSONObject().apply {
                put("result", jsonArray)
            }

        // Act
        val response = IssuingBankResultDataResponse.deserializer.deserialize(json)

        // Assert
        assertEquals(2, response.result.size)

        assertEquals("bank_id_123", response.result[0].id)
        assertEquals("Bank ABC", response.result[0].name)
        assertEquals(false, response.result[0].disabled)
        assertEquals("http://icon.url", response.result[0].iconUrl)

        assertEquals("bank_id_456", response.result[1].id)
        assertEquals("Bank XYZ", response.result[1].name)
        assertEquals(true, response.result[1].disabled)
        assertEquals("http://icon.xyz", response.result[1].iconUrl)
    }
}
