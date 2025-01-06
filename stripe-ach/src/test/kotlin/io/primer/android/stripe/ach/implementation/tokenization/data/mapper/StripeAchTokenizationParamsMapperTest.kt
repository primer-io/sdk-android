package io.primer.android.stripe.ach.implementation.tokenization.data.mapper

import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.model.toTokenizationRequest
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchSessionInfoDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StripeAchTokenizationParamsMapperTest {
    private val mapper = StripeAchTokenizationParamsMapper()

    @Test
    fun `map() correctly maps StripeAchPaymentInstrumentParams to StripeAchPaymentInstrumentDataRequest`() {
        val params =
            TokenizationParams(
                StripeAchPaymentInstrumentParams(
                    paymentMethodConfigId = "id",
                    locale = "en_US",
                ),
                sessionIntent = PrimerSessionIntent.CHECKOUT,
            )

        val result = mapper.map(params)

        val expectedInstrumentDataRequest =
            StripeAchPaymentInstrumentDataRequest(
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                paymentMethodConfigId = "id",
                sessionInfo = StripeAchSessionInfoDataRequest(locale = "en_US"),
                type = PaymentInstrumentType.AUTOMATED_CLEARING_HOUSE,
            )
        val expectedTokenizationRequest =
            expectedInstrumentDataRequest.toTokenizationRequest(PrimerSessionIntent.CHECKOUT)

        assertEquals(expectedTokenizationRequest.paymentInstrument, result.paymentInstrument)
    }
}
