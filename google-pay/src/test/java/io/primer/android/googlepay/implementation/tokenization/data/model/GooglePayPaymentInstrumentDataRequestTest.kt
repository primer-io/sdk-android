package io.primer.android.googlepay.implementation.tokenization.data.model

import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayFlow
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GooglePayPaymentInstrumentDataRequestTest {
    @Test
    fun `GooglePayPaymentInstrumentDataRequest should serialize correctly`() {
        // Given
        val merchantId = "merchant123"
        val encryptedPayload = "encryptedPayload"
        val flow = GooglePayFlow.GATEWAY
        val dataRequest =
            GooglePayPaymentInstrumentDataRequest(
                merchantId = merchantId,
                encryptedPayload = encryptedPayload,
                flow = flow,
            )

        // When
        val json = GooglePayPaymentInstrumentDataRequest.serializer.serialize(dataRequest)

        // Then
        val expectedJson =
            JSONObject().apply {
                put("merchantId", merchantId)
                put("encryptedPayload", encryptedPayload)
                put("flow", flow.name)
            }

        assertEquals(expectedJson.toString(), json.toString())
    }
}
