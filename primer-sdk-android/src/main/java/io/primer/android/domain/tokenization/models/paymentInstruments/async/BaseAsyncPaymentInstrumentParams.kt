package io.primer.android.domain.tokenization.models.paymentInstruments.async

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal open class BaseAsyncPaymentInstrumentParams(
    override val paymentMethodType: String,
    open val paymentMethodConfigId: String?,
    open val locale: String,
    open val type: PaymentInstrumentType
) : BasePaymentInstrumentParams(paymentMethodType)
