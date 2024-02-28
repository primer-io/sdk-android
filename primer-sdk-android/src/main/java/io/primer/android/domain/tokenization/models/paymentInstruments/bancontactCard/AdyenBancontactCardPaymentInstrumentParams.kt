package io.primer.android.domain.tokenization.models.paymentInstruments.bancontactCard

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.domain.tokenization.models.paymentInstruments.async.BaseAsyncPaymentInstrumentParams

@Suppress("LongParameterList")
internal class AdyenBancontactCardPaymentInstrumentParams(
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val locale: String,
    val redirectionUrl: String,
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cardholderName: String,
    val userAgent: String
) : BaseAsyncPaymentInstrumentParams(
    paymentMethodType,
    paymentMethodConfigId,
    locale,
    PaymentInstrumentType.CARD_OFF_SESSION_PAYMENT
)
