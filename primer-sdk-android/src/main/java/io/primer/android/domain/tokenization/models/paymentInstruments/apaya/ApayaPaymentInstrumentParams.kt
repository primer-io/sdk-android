package io.primer.android.domain.tokenization.models.paymentInstruments.apaya

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams

internal data class ApayaPaymentInstrumentParams(
    val mx: String,
    val mnc: String,
    val mcc: String,
    val hashedIdentifier: String,
    val productId: String,
    val currencyCode: String
) : BasePaymentInstrumentParams(PaymentMethodType.APAYA.name)
