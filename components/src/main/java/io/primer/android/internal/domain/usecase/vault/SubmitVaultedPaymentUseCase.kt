package io.primer.android.internal.domain.usecase.vault

import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.internal.domain.repositories.HeadlessRepository
import io.primer.android.internal.domain.repositories.PrimerVaultManagerRepository

internal class SubmitVaultedPaymentUseCase(
    private val vaultManagerRepository: PrimerVaultManagerRepository,
    private val headlessRepository: HeadlessRepository,
) {

    suspend operator fun invoke(
        vaultedPaymentMethodId: String,
        cvv: String? = null,
    ): Result<PrimerCheckoutData> {
        val additionalData = cvv?.let { PrimerVaultedCardAdditionalData(it) }
        return vaultManagerRepository.startPaymentFlow(vaultedPaymentMethodId, additionalData)
            .fold(
                onSuccess = { headlessRepository.awaitPaymentResult() },
                onFailure = { Result.failure(it) },
            )
    }
}
