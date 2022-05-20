package io.primer.android.domain.tokenization.models

import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.domain.base.Params
import io.primer.android.payment.PaymentMethodDescriptor

internal data class TokenizationParams(
    val paymentMethodDescriptor: PaymentMethodDescriptor,
    val paymentMethodIntent: PrimerPaymentMethodIntent,
    val is3DSOnVaultingEnabled: Boolean,
) : Params
