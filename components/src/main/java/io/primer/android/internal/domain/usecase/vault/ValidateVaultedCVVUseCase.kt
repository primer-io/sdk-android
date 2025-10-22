package io.primer.android.internal.domain.usecase.vault

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData

/**
 * Use case for validating CVV and other additional data for vaulted payment methods.
 * Performs client-side validation before submitting the payment.
 */
internal class ValidateVaultedCVVUseCase(
    private val vaultManagerRepository: PrimerVaultManagerRepository,
) {

    suspend operator fun invoke(
        vaultedPaymentMethodId: String,
        additionalData: PrimerVaultedPaymentMethodAdditionalData,
    ): Result<List<PrimerValidationError>> {
        return vaultManagerRepository.validate(vaultedPaymentMethodId, additionalData)
    }
}
