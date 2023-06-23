package io.primer.android.data.payments.methods.mapping.vault.card

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.data.payments.methods.mapping.vault.VaultedPaymentMethodAdditionalDataMapper
import io.primer.android.data.payments.methods.models.card.CardVaultExchangeDataRequest

internal class VaultedCardAdditionalDataMapper :
    VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedCardAdditionalData> {
    override fun map(t: PrimerVaultedCardAdditionalData) = CardVaultExchangeDataRequest(t.cvv)
}
