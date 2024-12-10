package io.primer.android.clientSessionActions.domain.models

import io.primer.android.configuration.data.model.CountryCode
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PrimerPhoneCodeTest {

    @Test
    fun `deserializer correctly parses valid JSON`() {
        val json = """
            {
                "name": "United States",
                "code": "US",
                "dial_code": "+1"
            }
        """.trimIndent()

        val jsonObject = JSONObject(json)
        val primerPhoneCode = PrimerPhoneCode.deserializer.deserialize(jsonObject)

        assertEquals("United States", primerPhoneCode.name)
        assertEquals(CountryCode.US, primerPhoneCode.code)
        assertEquals("+1", primerPhoneCode.dialCode)
    }

    @Test
    fun `default value is correct`() {
        val defaultPrimerPhoneCode = PrimerPhoneCode.default

        assertEquals("United Kingdom", defaultPrimerPhoneCode.name)
        assertEquals(CountryCode.BG, defaultPrimerPhoneCode.code)
        assertEquals("+44", defaultPrimerPhoneCode.dialCode)
    }
}
