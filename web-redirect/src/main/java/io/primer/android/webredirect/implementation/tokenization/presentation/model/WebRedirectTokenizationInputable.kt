package io.primer.android.webredirect.implementation.tokenization.presentation.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class WebRedirectTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
