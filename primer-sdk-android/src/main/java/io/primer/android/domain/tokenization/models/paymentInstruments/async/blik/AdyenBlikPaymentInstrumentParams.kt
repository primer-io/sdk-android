package io.primer.android.domain.tokenization.models.paymentInstruments.async.blik

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal data class AdyenBlikPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    override val redirectionUrl: String,
    val blikCode: String,
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT,
    redirectionUrl
)
