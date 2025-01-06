package io.primer.android.stripe.ach.implementation.tokenization.domain.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BaseAsyncPaymentInstrumentParams

internal data class StripeAchPaymentInstrumentParams(
    override val paymentMethodConfigId: String,
    override val locale: String,
) : BaseAsyncPaymentInstrumentParams(
        paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
        paymentMethodConfigId = paymentMethodConfigId,
        locale = locale,
        type = PaymentInstrumentType.AUTOMATED_CLEARING_HOUSE,
    ) {
    val authenticationProvider = "STRIPE"
}
