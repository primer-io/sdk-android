package io.primer.android.bancontact.implementation.tokenization.domain.model

import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

internal data class AdyenBancontactPaymentInstrumentParams(
    override val paymentMethodType: String,
    val paymentMethodConfigId: String,
    val locale: String,
    val redirectionUrl: String,
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cardholderName: String,
    val userAgent: String,
) : BasePaymentInstrumentParams
