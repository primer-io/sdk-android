package io.primer.android.domain.tokenization.models.paymentInstruments.async.dummy

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams
import io.primer.android.payment.dummy.DummyDecisionType

internal data class PrimerDummyPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val redirectionUrl: String,
    val flowDecisionType: DummyDecisionType,
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT
)
