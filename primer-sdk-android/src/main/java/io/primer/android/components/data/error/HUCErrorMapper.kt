package io.primer.android.components.data.error

import io.primer.android.components.domain.exception.InvalidTokenizationDataException
import io.primer.android.data.error.DefaultErrorMapper
import io.primer.android.data.configuration.exception.MissingConfigurationException
import io.primer.android.domain.error.models.HUCError
import io.primer.android.domain.error.models.PrimerError

internal class HUCErrorMapper : DefaultErrorMapper() {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is InvalidTokenizationDataException -> HUCError.InvalidTokenizationInputDataError(
                throwable.paymentMethodType,
                throwable.inputData,
                throwable.requiredInputData
            )
            is MissingConfigurationException -> HUCError.MissingConfigurationError
            else -> super.getPrimerError(throwable)
        }
    }
}
