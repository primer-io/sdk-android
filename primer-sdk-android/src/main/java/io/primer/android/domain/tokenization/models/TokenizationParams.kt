package io.primer.android.domain.tokenization.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.base.Params
import io.primer.android.payment.PaymentMethodDescriptor

internal data class TokenizationParams(
    val paymentMethodDescriptor: PaymentMethodDescriptor,
    val paymentMethodIntent: PrimerSessionIntent,
    val is3DSOnVaultingEnabled: Boolean,
) : Params
