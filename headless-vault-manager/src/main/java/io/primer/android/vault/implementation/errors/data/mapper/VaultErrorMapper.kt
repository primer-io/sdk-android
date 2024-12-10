package io.primer.android.vault.implementation.errors.data.mapper

import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.vault.implementation.errors.domain.model.VaultManagerError

internal class VaultErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is InvalidVaultedPaymentMethodIdException ->
                VaultManagerError.InvalidVaultedPaymentMethodIdError(
                    message = throwable.message.orEmpty()
                )

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
