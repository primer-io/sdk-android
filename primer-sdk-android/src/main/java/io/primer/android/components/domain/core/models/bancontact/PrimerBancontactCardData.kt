package io.primer.android.components.domain.core.models.bancontact

import io.primer.android.components.domain.core.models.PrimerRawData

data class PrimerBancontactCardData(
    val cardNumber: String,
    val expiryDate: String,
    val cardHolderName: String
) : PrimerRawData
