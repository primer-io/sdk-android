package io.primer.android.components.currencyformat.data.models

import io.primer.android.components.currencyformat.domain.models.CurrencyFormat
import org.json.JSONArray
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CurrencyFormatDataResponseTest {

    private val currencyFormatDataResponse by lazy {
        CurrencyFormatDataResponse.deserializer.deserialize(
            JSONArray(JSON_ARRAY)
        )
    }

    @Test
    fun `currency formats should be deserialized correctly`() {
        val expectedFormats = listOf(
            CurrencyFormat("AED", 2),
            CurrencyFormat("AFN", 2),
            CurrencyFormat("ALL", 2),
            CurrencyFormat("AMD", 2),
            CurrencyFormat("ANG", 2),
            CurrencyFormat("AOA", 2)
        )
        assertEquals(expectedFormats, currencyFormatDataResponse.data)
    }

    private companion object {
        const val JSON_ARRAY = """
            [
                {"m":2,"c":"AED"},
                {"m":2,"c":"AFN"},
                {"m":2,"c":"ALL"},
                {"m":2,"c":"AMD"},
                {"m":2,"c":"ANG"},
                {"m":2,"c":"AOA"}
            ]
        """
    }
}
