package io.primer.android.banks.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BankIssuerPaymentInstrumentDataRequestTest {

    @Test
    fun `BankIssuerPaymentInstrumentDataRequest should serialize correctly`() {
        // Given
        val type = PaymentInstrumentType.OFF_SESSION_PAYMENT
        val paymentMethodType = "paymentMethodType"
        val paymentMethodConfigId = "paymentMethodConfigId"
        val sessionInfo = BankIssuerSessionInfoDataRequest(
            redirectionUrl = "redirectionUrl",
            locale = "locale",
            issuer = "issuer"
        )

        val dataRequest = BankIssuerPaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = paymentMethodConfigId,
            sessionInfo = sessionInfo,
            type = type
        )

        // When
        val json = BankIssuerPaymentInstrumentDataRequest.serializer.serialize(dataRequest)

        // Then
        val expectedJson = JSONObject().apply {
            put("type", type.name)
            put("paymentMethodType", paymentMethodType)
            put("paymentMethodConfigId", paymentMethodConfigId)
            put(
                "sessionInfo",
                JSONObject().apply {
                    put("redirectionUrl", sessionInfo.redirectionUrl)
                    put("locale", sessionInfo.locale)
                    put("redirectionUrl", sessionInfo.redirectionUrl)
                    put("platform", sessionInfo.platform)
                    put("issuer", sessionInfo.issuer)
                }
            )
        }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
