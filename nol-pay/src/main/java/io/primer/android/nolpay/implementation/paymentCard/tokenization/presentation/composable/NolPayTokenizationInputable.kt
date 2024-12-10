package io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class NolPayTokenizationInputable(
    val mobileNumber: String,
    val nolPayCardNumber: String,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable
