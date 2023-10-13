package io.primer.android.domain.tokenization.models.paymentInstruments.async.bankIssuer

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal data class BankIssuerPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    override val redirectionUrl: String,
    val bankIssuer: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT,
    redirectionUrl
)
