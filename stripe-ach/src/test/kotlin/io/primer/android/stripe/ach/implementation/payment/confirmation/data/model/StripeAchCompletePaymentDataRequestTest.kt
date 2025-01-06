package io.primer.android.stripe.ach.implementation.payment.confirmation.data.model

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class StripeAchCompletePaymentDataRequestTest {
    @Test
    fun `StripeAchCompletePaymentDataRequest serializer should return correct platform and locale`() {
        val request =
            StripeAchCompletePaymentDataRequest.serializer.serialize(
                StripeAchCompletePaymentDataRequest(
                    mandateTimestamp = "timestamp",
                    paymentMethodId = "paymentMethodId",
                ),
            )
        assertEquals("paymentMethodId", request.getString("paymentMethodId"))
        assertEquals("timestamp", request.getString("mandateSignatureTimestamp"))
    }
}
