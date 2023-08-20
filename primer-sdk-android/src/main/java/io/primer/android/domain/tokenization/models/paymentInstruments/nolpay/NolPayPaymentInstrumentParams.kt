package io.primer.android.domain.tokenization.models.paymentInstruments.nolpay

import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal class NolPayPaymentInstrumentParams(
    override val paymentMethodType: String,
    val skdId: String,
    val regionCode: String,
    val mobileNumber: String,
    val cardNumber: String,
) : BasePaymentInstrumentParams(paymentMethodType)
