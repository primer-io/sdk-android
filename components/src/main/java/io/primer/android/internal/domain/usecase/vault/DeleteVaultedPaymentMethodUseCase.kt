package io.primer.android.internal.domain.usecase.vault

import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository

internal class DeleteVaultedPaymentMethodUseCase(
    private val repository: PrimerVaultManagerRepository,
) {

    suspend operator fun invoke(paymentMethodId: String): Result<Unit> {
        return repository.deleteVaultedPaymentMethod(paymentMethodId)
    }
}
