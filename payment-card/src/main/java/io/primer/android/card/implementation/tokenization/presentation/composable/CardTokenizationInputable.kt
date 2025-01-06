package io.primer.android.card.implementation.tokenization.presentation.composable

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.payments.core.tokenization.presentation.composable.TokenizationInputable

internal data class CardTokenizationInputable(
    val cardData: PrimerCardData,
    override val paymentMethodType: String,
    override val primerSessionIntent: PrimerSessionIntent,
) : TokenizationInputable
