package io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules

import io.primer.android.components.SdkUninitializedException
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule

internal class SdkInitializedRule(private val configurationRepository: ConfigurationRepository) :
    ValidationRule<Any> {
    override fun validate(t: Any): ValidationResult {
        return runCatching {
            configurationRepository.getConfiguration()
        }.fold(
            { ValidationResult.Success },
            { ValidationResult.Failure(SdkUninitializedException()) },
        )
    }
}
