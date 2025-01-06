package io.primer.android.vouchers.multibanco

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo

data class MultibancoCheckoutAdditionalInfo(
    val expiresAt: String,
    val reference: String,
    val entity: String,
    override val completesCheckout: Boolean = true,
) : PrimerCheckoutAdditionalInfo
