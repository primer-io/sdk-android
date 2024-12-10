package io.primer.android.ipay88.implementation.errors.data.mapper

import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.domain.error.models.PrimerError
import io.primer.ipay88.api.exceptions.IPayConnectionErrorException
import io.primer.ipay88.api.exceptions.IPayPaymentFailedException
import io.primer.android.ipay88.implementation.errors.domain.model.IPay88Error

internal class IPayErrorMapper : ErrorMapper {

    override fun getPrimerError(throwable: Throwable): PrimerError {
        return when (throwable) {
            is IPayPaymentFailedException -> IPay88Error.IPaySdkPaymentFailedError(
                throwable.transactionId,
                throwable.refNo,
                throwable.errorDescription
            )

            is IPayConnectionErrorException -> IPay88Error.IPaySdkConnectionError
            else -> error("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
        }
    }
}
