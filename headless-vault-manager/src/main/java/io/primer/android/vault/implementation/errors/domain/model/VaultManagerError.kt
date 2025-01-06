package io.primer.android.vault.implementation.errors.domain.model

import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.domain.error.models.PrimerError
import java.util.UUID

internal sealed class VaultManagerError : PrimerError() {
    data class InvalidVaultedPaymentMethodIdError(val message: String) : VaultManagerError() {
        override val context = ErrorContextParams(errorId = errorId)
    }

    override val errorId: String
        get() =
            when (this) {
                is InvalidVaultedPaymentMethodIdError -> "invalid-vaulted-payment-method-id"
            }

    override val description: String
        get() =
            when (this) {
                is InvalidVaultedPaymentMethodIdError -> message
            }

    override val errorCode: String? = null

    override val diagnosticsId: String
        get() =
            when (this) {
                is InvalidVaultedPaymentMethodIdError -> UUID.randomUUID().toString()
            }

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() =
            when (this) {
                is InvalidVaultedPaymentMethodIdError ->
                    "Indicates that the specific vaulted payment method was not found. " +
                        "Ensure you are passing an ID that is still present in the vault."
            }
}
