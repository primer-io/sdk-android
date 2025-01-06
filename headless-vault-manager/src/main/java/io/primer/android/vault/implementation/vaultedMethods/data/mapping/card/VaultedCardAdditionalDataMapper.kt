package io.primer.android.vault.implementation.vaultedMethods.data.mapping.card

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.vault.implementation.vaultedMethods.data.mapping.VaultedPaymentMethodAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.model.card.CardVaultExchangeDataRequest

internal class VaultedCardAdditionalDataMapper :
    VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedCardAdditionalData> {
    override fun map(t: PrimerVaultedCardAdditionalData) = CardVaultExchangeDataRequest(cvv = t.cvv)
}
