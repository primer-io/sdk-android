package io.primer.android.klarna.implementation.session.data.models

import io.mockk.mockkStatic
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CreateCustomerTokenDataRequestTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `serializer should correctly serialize request`() {
        // Given
        val localeData = LocaleDataRequest(
            countryCode = CountryCode.US,
            currencyCode = "USD",
            localeCode = "en_US"
        )

        val request = CreateCustomerTokenDataRequest(
            paymentMethodConfigId = "config-id",
            sessionId = "session-id",
            authorizationToken = "auth-token",
            description = "description",
            localeData = localeData
        )

        val localeDataJson = JSONObject().apply {
            put("countryCode", "US")
            put("currencyCode", "USD")
            put("localeCode", "en_US")
        }

        val expectedJson = JSONObject().apply {
            put("paymentMethodConfigId", "config-id")
            put("sessionId", "session-id")
            put("authorizationToken", "auth-token")
            putOpt("description", "description")
            put("localeData", localeDataJson)
        }

        // When
        val actualJson = CreateCustomerTokenDataRequest.serializer.serialize(request)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `provider should correctly whitelist keys`() {
        // Given
        val expectedKeys = listOf(
            "paymentMethodConfigId",
            "sessionId",
            "localeData"
        )

        // When
        val whitelistedKeys = CreateCustomerTokenDataRequest.provider.values.map { it.value }

        // Then
        assertEquals(expectedKeys, whitelistedKeys)
    }
}
