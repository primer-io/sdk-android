package io.primer.android.payments.core.tokenization.domain.model.paymentInstruments

import io.primer.android.configuration.data.model.PaymentInstrumentType

open class BaseAsyncPaymentInstrumentParams(
    override val paymentMethodType: String,
    open val paymentMethodConfigId: String?,
    open val locale: String,
    open val type: PaymentInstrumentType
) : BasePaymentInstrumentParams
