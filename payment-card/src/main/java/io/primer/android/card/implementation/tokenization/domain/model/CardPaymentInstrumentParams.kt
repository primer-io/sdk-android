package io.primer.android.card.implementation.tokenization.domain.model

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

internal data class CardPaymentInstrumentParams(
    override val paymentMethodType: String,
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cvv: String,
    val cardholderName: String?,
    val preferredNetwork: CardNetwork.Type?,
) : BasePaymentInstrumentParams
