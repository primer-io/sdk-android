package io.primer.android.vault.implementation.vaultedMethods.domain.repository

import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

internal fun interface VaultedPaymentMethodExchangeRepository {
    suspend fun exchangeVaultedPaymentToken(
        id: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?,
    ): Result<PaymentMethodTokenInternal>
}
