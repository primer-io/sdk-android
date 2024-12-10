package io.primer.android.vault.implementation.vaultedMethods.domain.validation.additionalData

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

internal fun interface VaultedPaymentMethodAdditionalDataValidator<
    out T : PrimerVaultedPaymentMethodAdditionalData> {

    suspend fun validate(
        additionalData: @UnsafeVariance T,
        vaultedPaymentMethodData: PrimerVaultedPaymentMethod
    ): List<PrimerValidationError>
}
