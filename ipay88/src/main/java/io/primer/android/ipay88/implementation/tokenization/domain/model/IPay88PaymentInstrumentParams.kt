package io.primer.android.ipay88.implementation.tokenization.domain.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BaseAsyncPaymentInstrumentParams

internal class IPay88PaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT,
)
