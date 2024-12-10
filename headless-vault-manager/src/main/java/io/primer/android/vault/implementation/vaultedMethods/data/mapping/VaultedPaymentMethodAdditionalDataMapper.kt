package io.primer.android.vault.implementation.vaultedMethods.data.mapping

import io.primer.android.vault.implementation.vaultedMethods.data.model.BasePaymentMethodVaultExchangeDataRequest
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

internal fun interface VaultedPaymentMethodAdditionalDataMapper<
    out T : PrimerVaultedPaymentMethodAdditionalData?> {

    fun map(t: @UnsafeVariance T): BasePaymentMethodVaultExchangeDataRequest
}
