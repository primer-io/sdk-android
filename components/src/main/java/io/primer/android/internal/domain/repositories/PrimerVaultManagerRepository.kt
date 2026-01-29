package io.primer.android.internal.domain.repositories

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

internal interface PrimerVaultManagerRepository {
    suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethod>>
    suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ): Result<List<PrimerValidationError>>
    suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData? = null,
    ): Result<Unit>
    suspend fun deleteVaultedPaymentMethod(paymentMethodId: String): Result<Unit>
}
