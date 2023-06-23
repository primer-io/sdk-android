package io.primer.android.data.payments.methods.mapping.vault.default

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.data.payments.methods.mapping.vault.VaultedPaymentMethodAdditionalDataMapper
import io.primer.android.data.payments.methods.models.empty.EmptyExchangeDataRequest

internal class EmptyAdditionalDataMapper :
    VaultedPaymentMethodAdditionalDataMapper<PrimerVaultedPaymentMethodAdditionalData?> {
    override fun map(t: PrimerVaultedPaymentMethodAdditionalData?) = EmptyExchangeDataRequest()
}
