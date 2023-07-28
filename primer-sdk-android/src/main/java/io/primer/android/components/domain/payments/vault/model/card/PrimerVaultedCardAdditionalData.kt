package io.primer.android.components.domain.payments.vault.model.card

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData

data class PrimerVaultedCardAdditionalData(val cvv: String) :
    PrimerVaultedPaymentMethodAdditionalData
