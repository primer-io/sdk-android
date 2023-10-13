package io.primer.android.domain.tokenization.models.paymentInstruments.async.webRedirect

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal class WebRedirectPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    override val redirectionUrl: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT,
    redirectionUrl
)
