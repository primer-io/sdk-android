package io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules

import io.primer.android.components.domain.exception.VaultManagerInitException
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule

internal class ValidClientSessionCustomerIdRule(
    private val configurationRepository: ConfigurationRepository
) : ValidationRule<Any> {

    override fun validate(t: Any): ValidationResult {
        return runCatching {
            configurationRepository.getConfiguration().clientSession
                .clientSessionDataResponse.customerId.isNullOrBlank()
                .not()
        }.fold(
            { ValidationResult.Success },
            { ValidationResult.Failure(VaultManagerInitException(MISSING_CUSTOMER_ID_MESSAGE)) }
        )
    }

    private companion object {

        const val MISSING_CUSTOMER_ID_MESSAGE =
            "You must provide a `customer.customerId` in the client session"
    }
}
