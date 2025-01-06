package io.primer.android.ipay88.implementation.tokenization.presentation.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class IPay88TokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
