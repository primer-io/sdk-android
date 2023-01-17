package io.primer.android.domain.tokenization.models.paymentInstruments.paypal

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal class PaypalCheckoutPaymentInstrumentParams(
    val paypalOrderId: String?,
    val externalPayerInfoEmail: String?,
) : BasePaymentInstrumentParams(PaymentMethodType.PAYPAL.name)
