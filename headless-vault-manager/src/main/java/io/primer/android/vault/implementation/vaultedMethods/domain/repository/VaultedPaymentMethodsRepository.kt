package io.primer.android.vault.implementation.vaultedMethods.domain.repository

import io.primer.android.vault.implementation.vaultedMethods.data.model.PaymentMethodVaultTokenInternal

internal interface VaultedPaymentMethodsRepository {
    // upon refactor of fragments, convert to domain model!
    suspend fun getVaultedPaymentMethods(fromCache: Boolean): Result<List<PaymentMethodVaultTokenInternal>>

    suspend fun deleteVaultedPaymentMethod(id: String): Result<Unit>
}
