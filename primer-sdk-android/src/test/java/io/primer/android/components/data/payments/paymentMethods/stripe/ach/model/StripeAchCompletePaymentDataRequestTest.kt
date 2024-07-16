package io.primer.android.components.data.payments.paymentMethods.stripe.ach.model

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
                StripeAchCompletePaymentDataRequest("timestamp", "paymentMethodId")
            )
        assertEquals("paymentMethodId", request.getString("paymentMethodId"))
        assertEquals("timestamp", request.getString("mandateSignatureTimestamp"))
    }
}
