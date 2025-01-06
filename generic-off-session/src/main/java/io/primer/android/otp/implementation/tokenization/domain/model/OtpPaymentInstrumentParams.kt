package io.primer.android.otp.implementation.tokenization.domain.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BaseAsyncPaymentInstrumentParams

internal data class OtpPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val otp: String,
) : BaseAsyncPaymentInstrumentParams(
        paymentMethodType,
        paymentMethodConfigId,
        locale,
        PaymentInstrumentType.OFF_SESSION_PAYMENT,
    )
