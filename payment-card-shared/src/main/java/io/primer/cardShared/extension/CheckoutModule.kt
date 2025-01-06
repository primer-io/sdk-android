package io.primer.cardShared.extension

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.inputs.models.isEnabled
import io.primer.android.configuration.domain.model.CheckoutModule

fun CheckoutModule.CardInformation?.isCardHolderNameEnabled() =
    when {
        this == null -> true
        options.isNullOrEmpty() -> true
        options.isEnabled(PrimerInputElementType.ALL) -> true
        options.isEnabled(PrimerInputElementType.CARDHOLDER_NAME) -> true
        else -> false
    }
