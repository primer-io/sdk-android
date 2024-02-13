package io.primer.android.domain.tokenization.models.paymentInstruments.card

import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.ui.CardNetwork

internal class CardPaymentInstrumentParams(
    override val paymentMethodType: String,
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cvv: String,
    val cardholderName: String?,
    val preferredNetwork: CardNetwork.Type?
) : BasePaymentInstrumentParams(paymentMethodType)
