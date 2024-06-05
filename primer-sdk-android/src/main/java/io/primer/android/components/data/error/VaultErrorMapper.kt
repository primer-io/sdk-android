package io.primer.android.components.data.error

import io.primer.android.components.domain.exception.InvalidVaultedPaymentMethodIdException
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.error.models.VaultManagerError

internal class VaultErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        if (throwable is InvalidVaultedPaymentMethodIdException) {
            return VaultManagerError.InvalidVaultedPaymentMethodIdError(
                message = throwable.message.orEmpty()
            )
        }
        return super.getPrimerError(throwable)
    }
}
