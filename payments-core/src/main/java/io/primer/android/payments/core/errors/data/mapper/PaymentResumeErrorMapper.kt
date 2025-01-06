package io.primer.android.payments.core.errors.data.mapper

import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.ErrorMapper
import io.primer.android.errors.domain.models.HttpError
import io.primer.android.payments.core.errors.data.exception.PaymentResumeException
import io.primer.android.payments.core.errors.domain.model.PaymentError

internal class PaymentResumeErrorMapper : ErrorMapper {
    override fun getPrimerError(throwable: Throwable): PrimerError {
        when (throwable) {
            is PaymentResumeException -> {
                val cause = throwable.cause
                if (cause is HttpException && (cause.isClientError() || cause.isPaymentError())) {
                    return HttpError.HttpClientError(
                        cause.errorCode,
                        cause.error.diagnosticsId,
                        cause.error.description,
                        PaymentError.PaymentResumeFailedError(
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
