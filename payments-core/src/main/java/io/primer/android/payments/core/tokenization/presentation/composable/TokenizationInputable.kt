package io.primer.android.payments.core.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent

interface TokenizationInputable {
    val paymentMethodType: String
    val primerSessionIntent: PrimerSessionIntent
}
