package io.primer.android.domain.tokenization.models.paymentInstruments.async.retailOutlet

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal data class XenditRetailOutletPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val redirectionUrl: String,
    val retailOutlet: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT
)
