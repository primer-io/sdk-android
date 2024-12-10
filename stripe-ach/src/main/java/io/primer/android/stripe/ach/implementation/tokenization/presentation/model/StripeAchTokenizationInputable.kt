package io.primer.android.stripe.ach.implementation.tokenization.presentation.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class StripeAchTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable
