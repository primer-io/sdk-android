package io.primer.android.domain.payments.additionalInfo

interface PrimerCheckoutAdditionalInfo

data class MultibancoCheckoutAdditionalInfo(
    val expiresAt: String,
    val reference: String,
    val entity: String,
) : PrimerCheckoutAdditionalInfo
