package io.primer.android.banks.implementation.tokenization.presentation.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class BankIssuerTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
    val bankIssuer: String
) : TokenizationInputable
