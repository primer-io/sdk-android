package io.primer.android.components.domain.payments.vault.validation.additionalData

import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.components.domain.payments.vault.PrimerVaultedPaymentMethodAdditionalData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod

internal fun interface VaultedPaymentMethodAdditionalDataValidator<
    out T : PrimerVaultedPaymentMethodAdditionalData> {

    suspend fun validate(
        t: @UnsafeVariance T,
        vaultedPaymentMethodData: PrimerVaultedPaymentMethod
    ): List<PrimerValidationError>
}
