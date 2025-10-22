package io.primer.android.internal.domain.usecase.vault

import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository

/**
 * Use case for fetching vaulted payment methods from the vault manager.
 */
internal class FetchVaultedPaymentMethodsUseCase(
    private val vaultManagerRepository: PrimerVaultManagerRepository,
) {

    suspend operator fun invoke(): Result<List<PrimerVaultedPaymentMethod>> {
        return vaultManagerRepository.fetchVaultedPaymentMethods()
    }
}
