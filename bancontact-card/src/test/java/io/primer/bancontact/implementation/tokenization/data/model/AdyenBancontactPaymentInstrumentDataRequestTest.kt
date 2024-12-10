package io.primer.bancontact.implementation.tokenization.data.model

import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactSessionInfoDataRequest
import io.primer.android.configuration.data.model.PaymentInstrumentType
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AdyenBancontactPaymentInstrumentDataRequestTest {

    @Test
    fun `AydenBancontactPaymentInstrumentDataRequest should serialize correctly`() {
        // Given
        val type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        val paymentMethodType = "paymentMethodType"
        val paymentMethodConfigId = "paymentMethodConfigId"
        val sessionInfo = AdyenBancontactSessionInfoDataRequest(
            redirectionUrl = "redirectionUrl",
            locale = "locale",
            userAgent = "userAgent"
        )

        val dataRequest = AdyenBancontactPaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = paymentMethodConfigId,
            sessionInfo = sessionInfo,
            type = type,
            number = "number",
            expirationMonth = "expirationMonth",
            expirationYear = "expirationYear",
            cardholderName = "cardholderName"
        )

        // When
        val json = AdyenBancontactPaymentInstrumentDataRequest.serializer.serialize(dataRequest)

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
                    put("redirectionUrl", sessionInfo.redirectionUrl)
                    put(
                        "browserInfo",
                        JSONObject().apply {
                            put("userAgent", sessionInfo.userAgent)
                        }
                    )
                }
            )
            put("number", "number")
            put("expirationMonth", "expirationMonth")
            put("expirationYear", "expirationYear")
            put("cardholderName", "cardholderName")
        }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
