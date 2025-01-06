package io.primer.android.payments.core.errors.data.mapper

import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.payments.core.errors.data.exception.PaymentCreateException
import io.primer.android.payments.core.errors.domain.model.PaymentError

internal class PaymentCreateErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        when (throwable) {
            is PaymentCreateException -> {
                val cause = throwable.cause
                if (cause is HttpException && (cause.isClientError() || cause.isPaymentError())) {
                    return HttpError.HttpClientError(
                        cause.errorCode,
                        cause.error.diagnosticsId,
                        cause.error.description,
                        PaymentError.PaymentCreateFailedError(
                            cause.error.description,
                            cause.error.diagnosticsId,
                        ),
                    )
                }
            }
        }

        throw IllegalArgumentException("Unsupported mapping for $throwable in ${this.javaClass.canonicalName}")
    }
}
