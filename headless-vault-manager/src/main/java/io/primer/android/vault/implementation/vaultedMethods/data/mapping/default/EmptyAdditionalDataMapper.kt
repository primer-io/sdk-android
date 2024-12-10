package io.primer.android.vault.implementation.vaultedMethods.data.mapping.default

import io.primer.android.vault.implementation.vaultedMethods.data.mapping.VaultedPaymentMethodAdditionalDataMapper
import io.primer.android.vault.implementation.vaultedMethods.data.model.empty.EmptyExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

internal class EmptyAdditionalDataMapper :
    VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?> {
    override fun map(t: PrimerVaultedPaymentMethodAdditionalData?) = EmptyExchangeDataRequest()
}
