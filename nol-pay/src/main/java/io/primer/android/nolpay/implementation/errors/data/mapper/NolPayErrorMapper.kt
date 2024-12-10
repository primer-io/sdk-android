package io.primer.android.nolpay.implementation.errors.data.mapper

import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.nolpay.api.exceptions.NolPaySdkException
import io.primer.android.nolpay.implementation.errors.domain.model.NolPayError

internal class NolPayErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is NolPaySdkException -> NolPayError(
                errorCode = throwable.errorCode,
                errorMessage = throwable.message
            )

            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
