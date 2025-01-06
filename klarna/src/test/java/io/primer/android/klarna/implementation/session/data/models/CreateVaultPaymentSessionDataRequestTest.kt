package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.configuration.data.model.CountryCode
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CreateVaultPaymentSessionDataRequestTest {
    @Test
    fun `serializer should correctly serialize request`() {
        // Given
        val request =
            CreateVaultPaymentSessionDataRequest(
                paymentMethodConfigId = "config-id",
                sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                description = "description",
                localeData =
                    LocaleDataRequest(
                        countryCode = CountryCode.US,
                        currencyCode = "USD",
                        localeCode = "en_US",
                    ),
            )

        val localeDataJson =
            JSONObject().apply {
                put("countryCode", CountryCode.US.name)
                put("currencyCode", "USD")
                put("localeCode", "en_US")
            }

        val expectedJson =
            JSONObject().apply {
                put("paymentMethodConfigId", "config-id")
                put("sessionType", KlarnaSessionType.ONE_OFF_PAYMENT.name)
                putOpt("description", "description")
                put("localeData", localeDataJson)
            }

        // When
        val actualJson = CreateVaultPaymentSessionDataRequest.serializer.serialize(request)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `provider should correctly whitelist keys`() {
        // Given
        val expectedKeys =
            listOf(
                "paymentMethodConfigId",
                "sessionType",
                "localeData",
            )

        // When
        val whitelistedKeys = CreateVaultPaymentSessionDataRequest.provider.values.map { it.value }

        // Then
        assertEquals(expectedKeys, whitelistedKeys)
    }
}
