package io.primer.android.klarna.implementation.tokenization.domain.model

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData

internal data class KlarnaCheckoutPaymentInstrumentParams(
    val klarnaAuthorizationToken: String?,
    val sessionData: KlarnaSessionData,
    override val paymentMethodType: String = PaymentMethodType.KLARNA.name
) : KlarnaPaymentInstrumentParams()
