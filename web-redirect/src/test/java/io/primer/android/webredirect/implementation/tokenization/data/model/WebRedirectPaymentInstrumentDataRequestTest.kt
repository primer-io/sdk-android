package io.primer.android.webredirect.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WebRedirectPaymentInstrumentDataRequestTest {
    @Test
    fun `WebRedirectPaymentInstrumentDataRequest should serialize correctly`() {
        // Given
        val type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        val paymentMethodType = "paymentMethodType"
        val paymentMethodConfigId = "paymentMethodConfigId"
        val sessionInfo =
            WebRedirectSessionInfoDataRequest(
                redirectionUrl = "redirectionUrl",
                locale = "locale",
            )

        val dataRequest =
            WebRedirectPaymentInstrumentDataRequest(
                paymentMethodType = paymentMethodType,
                paymentMethodConfigId = paymentMethodConfigId,
                sessionInfo = sessionInfo,
                type = type,
            )

        // When
        val json = WebRedirectPaymentInstrumentDataRequest.serializer.serialize(dataRequest)

        // Then
        val expectedJson =
            JSONObject().apply {
                put("type", type.name)
                put("paymentMethodType", paymentMethodType)
                put("paymentMethodConfigId", paymentMethodConfigId)
                put(
                    "sessionInfo",
                    JSONObject().apply {
                        put("redirectionUrl", sessionInfo.redirectionUrl)
                        put("locale", sessionInfo.locale)
                        put("platform", sessionInfo.platform)
                    },
                )
            }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
