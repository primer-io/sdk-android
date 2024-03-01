package io.primer.android.domain.tokenization.models.paymentInstruments.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionData
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal data class KlarnaCheckoutPaymentInstrumentParams(
    val klarnaAuthorizationToken: String?,
    val sessionData: KlarnaSessionData
) : BasePaymentInstrumentParams(PaymentMethodType.KLARNA.name)
