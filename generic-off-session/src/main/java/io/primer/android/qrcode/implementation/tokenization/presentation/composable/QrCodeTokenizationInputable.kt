package io.primer.android.qrcode.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class QrCodeTokenizationInputable(
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable
