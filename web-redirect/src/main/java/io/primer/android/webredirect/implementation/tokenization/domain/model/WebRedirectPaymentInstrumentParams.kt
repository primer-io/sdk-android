package io.primer.android.webredirect.implementation.tokenization.domain.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BaseAsyncPaymentInstrumentParams

internal class WebRedirectPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val redirectionUrl: String,
    val platform: String,
) : BaseAsyncPaymentInstrumentParams(
        paymentMethodType,
        paymentMethodConfigId,
        locale,
        PaymentInstrumentType.OFF_SESSION_PAYMENT,
    )
