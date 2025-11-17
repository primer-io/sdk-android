package io.primer.android.internal.data.repositories

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManager
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManagerInterface
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

/**
 * Concrete implementation that lazily creates and caches the vault manager instance.
 */
internal class PrimerVaultManagerRepositoryImpl(
    private val manager: PrimerHeadlessUniversalCheckoutVaultManagerInterface =
        PrimerHeadlessUniversalCheckoutVaultManager.newInstance(),
) : PrimerVaultManagerRepository {

    override suspend fun fetchVaultedPaymentMethods(): Result<List<PrimerVaultedPaymentMethod>> =
        manager.fetchVaultedPaymentMethods()

    override suspend fun validate(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ): Result<List<PrimerValidationError>> =
        manager.validate(vaultedPaymentMethodId, additionalData)

    override suspend fun startPaymentFlow(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?,
    ): Result<Unit> = if (additionalData != null) {
        manager.startPaymentFlow(vaultedPaymentMethodId, additionalData)
    } else {
        manager.startPaymentFlow(vaultedPaymentMethodId)
    }

    override suspend fun deleteVaultedPaymentMethod(paymentMethodId: String): Result<Unit> =
        manager.deleteVaultedPaymentMethod(paymentMethodId)
}
