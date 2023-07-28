package io.primer.android.domain.tokenization.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.base.Params
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.payment.PaymentMethodDescriptor

internal data class TokenizationParams(
    val paymentMethodDescriptor: PaymentMethodDescriptor,
    val paymentMethodIntent: PrimerSessionIntent,
) : Params

internal data class TokenizationParamsV2(
    val paymentInstrumentParams: BasePaymentInstrumentParams,
    val paymentMethodIntent: PrimerSessionIntent,
) : Params
