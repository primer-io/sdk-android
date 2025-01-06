package io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class MultibancoTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
