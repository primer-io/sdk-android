package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules

import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.domain.session.repository.ConfigurationRepository

internal class SdkInitializedRule(private val configurationRepository: ConfigurationRepository) :
    ValidationRule<Any> {
    override fun validate(t: Any): ValidationResult {
        return runCatching {
            configurationRepository.getConfiguration()
        }.fold(
            { ValidationResult.Success },
            { ValidationResult.Failure(SdkUninitializedException()) }
        )
    }
}
