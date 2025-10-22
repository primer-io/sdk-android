package io.primer.android.internal.domain.usecase.vault

import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

/**
 * Use case for submitting a vaulted payment method for processing.
 * Handles the payment flow including potential CVV recapture requirements.
 */
internal class SubmitVaultedPaymentUseCase(
    private val vaultManagerRepository: PrimerVaultManagerRepository,
) {

    suspend operator fun invoke(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData? = null,
    ): Result<Unit> {
        return vaultManagerRepository.startPaymentFlow(vaultedPaymentMethodId, additionalData)
    }
}
