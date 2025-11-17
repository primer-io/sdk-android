package io.primer.android.internal.domain.usecase.vault

import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository

/**
 * Use case for deleting a vaulted payment method.
 *
 * This operation removes the payment method from the vault permanently.
 */
internal class DeleteVaultedPaymentMethodUseCase(
    private val repository: PrimerVaultManagerRepository,
) {
    /**
     * Deletes a vaulted payment method by ID.
     *
     * @param paymentMethodId The ID of the payment method to delete
     * @return Result<Unit> Success if deleted, Failure with exception otherwise
     */
    suspend operator fun invoke(paymentMethodId: String): Result<Unit> {
        return repository.deleteVaultedPaymentMethod(paymentMethodId)
    }
}
