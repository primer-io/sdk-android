package io.primer.android.clientSessionActions.domain.models

import io.primer.android.configuration.data.model.CountryCode
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PrimerCountryTest {

    @Test
    fun `deserializer correctly parses valid JSON`() {
        val json = """
            {
                "name": "United States",
                "code": "US"
            }
        """.trimIndent()

        val jsonObject = JSONObject(json)
        val primerCountry = PrimerCountry.deserializer.deserialize(jsonObject)

        assertEquals("United States", primerCountry.name)
        assertEquals(CountryCode.US, primerCountry.code)
    }

    @Test
    fun `default value is correct`() {
        val defaultPrimerCountry = PrimerCountry.default

        assertEquals("United Kingdom", defaultPrimerCountry.name)
        assertEquals(CountryCode.BG, defaultPrimerCountry.code)
    }
}

internal class PrimerCountriesCodeInfoTest {

    @Test
    fun `deserializer correctly parses valid JSON`() {
        val json = """
            {
                "locale": "en-US",
                "countries": {
                    "US": {
                        "name": "United States",
                        "dialCode": "+1"
                    },
                    "GB": {
                        "name": "United Kingdom",
                        "dialCode": "+44"
                    }
                }
            }
        """.trimIndent()

        val jsonObject = JSONObject(json)
        val primerCountriesCodeInfo = PrimerCountriesCodeInfo.deserializer.deserialize(jsonObject)

        assertEquals("en-US", primerCountriesCodeInfo.locale)
        assertEquals(2, primerCountriesCodeInfo.countries.size)

        val usInfo = primerCountriesCodeInfo.countries["US"] as Map<*, *>
        assertEquals("United States", usInfo["name"])
        assertEquals("+1", usInfo["dialCode"])

        val gbInfo = primerCountriesCodeInfo.countries["GB"] as Map<*, *>
        assertEquals("United Kingdom", gbInfo["name"])
        assertEquals("+44", gbInfo["dialCode"])
    }
}
