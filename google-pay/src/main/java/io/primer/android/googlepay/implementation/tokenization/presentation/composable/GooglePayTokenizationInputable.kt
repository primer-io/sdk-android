package io.primer.android.googlepay.implementation.tokenization.presentation.composable

import com.google.android.gms.wallet.PaymentData
import io.primer.android.PrimerSessionIntent
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class GooglePayTokenizationInputable(
    val paymentData: PaymentData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent
) : TokenizationInputable
