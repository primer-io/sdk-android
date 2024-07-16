package io.primer.android.domain.tokenization.models.paymentInstruments.stripe.ach

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal data class StripeAchPaymentInstrumentParams(
    override val paymentMethodConfigId: String,
    override val locale: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
    paymentMethodConfigId = paymentMethodConfigId,
    locale = locale,
    type = PaymentInstrumentType.AUTOMATED_CLEARING_HOUSE
) {
    val authenticationProvider = "STRIPE"
}
