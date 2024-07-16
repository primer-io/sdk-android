package io.primer.android.data.tokenization.models

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.tokenization.models.paymentInstruments.async.AsyncPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.stripe.ach.StripeAchSessionInfoDataRequest
import io.primer.android.domain.tokenization.models.paymentInstruments.stripe.ach.StripeAchPaymentInstrumentParams
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TokenizationRequestV2KtTest {

    @Test
    fun `toPaymentInstrumentData() should return AsyncPaymentInstrumentDataRequest when called with StripeAchPaymentInstrumentParams`() {
        val params = StripeAchPaymentInstrumentParams("paymentMethodConfigId", "locale")

        val data = params.toPaymentInstrumentData() as AsyncPaymentInstrumentDataRequest

        assertEquals(PaymentMethodType.STRIPE_ACH.name, data.paymentMethodType)
        assertEquals("paymentMethodConfigId", data.paymentMethodConfigId)
        assertEquals(StripeAchSessionInfoDataRequest("locale"), data.sessionInfo)
        assertEquals(PaymentInstrumentType.AUTOMATED_CLEARING_HOUSE, data.type)
        assertEquals("STRIPE", data.authenticationProvider)
    }
}
