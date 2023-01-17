package io.primer.android.domain.tokenization.models.paymentInstruments.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal data class KlarnaPaymentInstrumentParams(
    val klarnaCustomerToken: String?,
    val sessionData: CreateCustomerTokenDataResponse.SessionData
) : BasePaymentInstrumentParams(PaymentMethodType.KLARNA.name)
