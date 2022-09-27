package io.primer.android.components.domain.core.models

import io.primer.android.data.configuration.models.PaymentInstrumentType

internal class PrimerCardAsyncRawDataTokenizationHelper(redirectionUrl: String) :
    PrimerAsyncRawDataTokenizationHelper(redirectionUrl) {

    override val paymentInstrumentType = PaymentInstrumentType.CARD_OFF_SESSION_PAYMENT
}
