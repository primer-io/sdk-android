package io.primer.android.bancontact.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class AdyenBancontactTokenizationInputable(
    val cardData: PrimerBancontactCardData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
