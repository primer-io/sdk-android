package io.primer.android.domain.tokenization.models.paymentInstruments.nolpay

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

internal class NolPayPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val mobileCountryCode: String,
    val mobileNumber: String,
    val nolPayCardNumber: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.OFF_SESSION_PAYMENT
)
