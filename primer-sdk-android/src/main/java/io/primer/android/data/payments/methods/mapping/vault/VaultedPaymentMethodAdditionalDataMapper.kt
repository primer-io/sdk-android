package io.primer.android.data.payments.methods.mapping.vault

import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.data.payments.methods.models.BasePaymentMethodVaultExchangeDataRequest

internal fun interface VaultedPaymentMethodAdditionalDataMapper<
    out T : PrimerVaultedPaymentMethodAdditionalData?> {

    fun map(t: @UnsafeVariance T): BasePaymentMethodVaultExchangeDataRequest
}
