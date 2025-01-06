package io.primer.android.payments.core.tokenization.domain.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.domain.Params
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams

data class TokenizationParams<T : BasePaymentInstrumentParams>(
    val paymentInstrumentParams: T,
    val sessionIntent: PrimerSessionIntent,
) : Params
