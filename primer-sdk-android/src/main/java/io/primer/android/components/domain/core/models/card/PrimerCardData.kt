package io.primer.android.components.domain.core.models.card

import io.primer.android.components.domain.core.models.PrimerRawData

data class PrimerCardData(
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val cardHolderName: String? = null,
) : PrimerRawData
