package io.primer.android.ipay88.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IPay88PaymentInstrumentDataRequestTest {

    @Test
    fun `IPay88PaymentInstrumentDataRequest should serialize correctly`() {
        // Given
        val type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        val paymentMethodType = "paymentMethodType"
        val paymentMethodConfigId = "paymentMethodConfigId"
        val sessionInfo = IPay88SessionInfoDataRequest(
            locale = "locale"
        )

        val dataRequest = IPay88PaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = paymentMethodConfigId,
            sessionInfo = sessionInfo,
            type = type
        )

        // When
        val json = IPay88PaymentInstrumentDataRequest.serializer.serialize(dataRequest)

        // Then
        val expectedJson = JSONObject().apply {
            put("type", type.name)
            put("paymentMethodType", paymentMethodType)
            put("paymentMethodConfigId", paymentMethodConfigId)
            put(
                "sessionInfo",
                JSONObject().apply {
                    put("locale", sessionInfo.locale)
                    put("platform", sessionInfo.platform)
                }
            )
        }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
