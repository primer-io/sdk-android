package io.primer.android.payments.core.create.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CreatePaymentDataRequestTest {
    @Test
    fun `test CreatePaymentDataRequest serialization`() {
        // Arrange
        val paymentMethodToken = "sample-token"
        val createPaymentDataRequest = CreatePaymentDataRequest(paymentMethodToken)
        val expectedJson =
            JSONObject().apply {
                put("paymentMethodToken", paymentMethodToken)
            }

        // Act
        val serializedJson = CreatePaymentDataRequest.serializer.serialize(createPaymentDataRequest)

        // Assert
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}
