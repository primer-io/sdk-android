package io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BaseAsyncPaymentInstrumentParams

internal data class NolPayPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val mobileCountryCode: String,
    val mobileNumber: String,
    val nolPayCardNumber: String,
) : BaseAsyncPaymentInstrumentParams(
        paymentMethodType,
        paymentMethodConfigId,
        locale,
        PaymentInstrumentType.OFF_SESSION_PAYMENT,
    )
